# Deploy guide — cloud VM shared with other projects

cnr runs as an isolated docker-compose stack that **never binds host 80/443**.
Its nginx (with SSE tuning) is published only on `127.0.0.1:${CNR_HTTP_PORT}`
(default `8082`). A **shared front reverse proxy** on the host terminates 80/443
and routes the cnr domain to that local port. This lets cnr coexist with any
other projects already on the VM.

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
