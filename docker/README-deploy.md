# Deploy guide — cloud VM shared with other projects

cnr coexists with an existing app (e.g. FastAPI on `:8000`) on the same VM.
DB (Supabase Postgres) and Redis (Aiven Valkey) are managed services, so only
two containers run here: `cnr-app` (Spring Boot) and `cnr-nginx` (SSE tuning).

## Quickstart — direct port on a 1GB VM (t2/t3.micro)

The current config publishes cnr-nginx on a **public port** (`CNR_HTTP_PORT`,
default `8081`), so cnr is reachable directly at `http://<vm-ip>:8081` next to
the existing app on `:8000`. No front proxy, no domain.

A 1GB instance running a Python app **and** a JVM needs swap or it will OOM.

```bash
# 1. Add 2GB swap (once per VM) — lets 1GB survive the JVM
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab   # persist across reboot
free -h                                                       # verify swap shows up

# 2. Deploy the cnr stack
cd ~/cnr/docker                 # DEPLOY_PATH/docker
cp .env.example .env            # fill secrets; keep CNR_HTTP_PORT=8081
docker compose -f docker-compose.prod.yml up -d --build

# 3. Verify locally on the VM
curl -i http://127.0.0.1:8081/swagger-ui/index.html
```

Then open port **8081** in the EC2 **security group** (inbound TCP, your IP or
0.0.0.0/0). Access from your browser: `http://<vm-ip>:8081/swagger-ui/index.html`.
JVM heap is capped at `-Xmx512m` in `.env`; the existing `:8000` app is untouched.

> Later, to share `:443`/HTTPS with a domain, switch to the reverse-proxy setup
> below (change nginx back to `127.0.0.1:8082` and add a front proxy).

---

## Alternative — shared front reverse proxy (domain + HTTPS)

cnr's nginx binds only `127.0.0.1:${CNR_HTTP_PORT}` and a **shared front reverse
proxy** on the host terminates 80/443 and routes the cnr domain to that local
port. Use this once you want a real domain and TLS.

```
Internet ──> :443 front reverse proxy ──┬─ other-project.com  -> 127.0.0.1:xxxx
   (host nginx / Caddy)                 └─ cnr.<domain>        -> 127.0.0.1:8082
                                                                     │
                                                              cnr-nginx (SSE)
                                                                     │
                                                                cnr-app:8080
```

## 0. First, inspect the VM

```bash
# What already listens on 80/443?
sudo lsof -i :80 -i :443 -nP | grep LISTEN
# Running containers and their host port mappings
docker ps --format 'table {{.Names}}\t{{.Ports}}'
# Is there a host nginx?
systemctl status nginx 2>/dev/null; nginx -v 2>&1
# Which local ports are already taken (avoid these for CNR_HTTP_PORT)
sudo ss -ltnp
```

Pick a `CNR_HTTP_PORT` (in `docker/.env`) that nothing else uses.

## 1. Deploy the cnr stack

```bash
cd ~/cnr/docker           # DEPLOY_PATH/docker
cp .env.example .env      # fill in real secrets + CNR_HTTP_PORT
docker compose -f docker-compose.prod.yml up -d --build
curl -i http://127.0.0.1:8082/actuator/health   # or any known endpoint
```

## 2. Front reverse proxy — pick ONE

### Option A — a host nginx already fronts the other projects
Add a server block (do **not** touch existing ones):

```nginx
# /etc/nginx/sites-available/cnr.conf  (or conf.d/cnr.conf)
server {
    listen 80;
    server_name cnr.<your-domain>;

    location / {
        proxy_pass http://127.0.0.1:8082;   # = CNR_HTTP_PORT
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # SSE pass-through (cnr-nginx already tunes upstream; keep buffering off here too)
        proxy_buffering off;
        proxy_read_timeout 3600s;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
```
```bash
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d cnr.<your-domain>     # HTTPS
```

### Option B — no shared proxy yet (set one up)
Easiest with automatic HTTPS is **Caddy** as the host-level front proxy:

```
# /etc/caddy/Caddyfile
cnr.<your-domain> {
    reverse_proxy 127.0.0.1:8082
}
# add other projects here later, one block each
```
```bash
sudo systemctl reload caddy   # Caddy fetches/renews TLS certs automatically
```
(If other projects currently bind :80 directly, move them behind this same
Caddy/nginx first — only one process can own :80/:443.)

## 3. DNS
Point `cnr.<your-domain>` A record at the VM's public IP.

## 4. App-side domain settings (edit in repo, redeploy)
- `module-adaptor/inbound/api/.../application-prod.yml` → `cors.allowedOrigins`
- `KAKAO_REDIRECT_URI` in `docker/.env` + Kakao developer console
