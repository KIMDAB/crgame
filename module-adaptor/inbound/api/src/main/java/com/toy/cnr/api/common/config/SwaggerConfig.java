package com.toy.cnr.api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger (OpenAPI 3.0) 설정.
 * <p>
 * Swagger UI 접속: <a href="http://localhost:8080/swagger-ui/index.html">http://localhost:8080/swagger-ui/index.html</a>
 * <br>
 * API Docs JSON: <a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a>
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cops N Robbers API")
                .description("REST API")
                .version("v1.0.0")
            )
            .servers(List.of(
                new Server().url("http://125.240.22.220").description("Dev"),
                new Server().url("https://api.cnr.example.com").description("Production")
            ))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                    .name(BEARER_AUTH)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                )
            );
    }
}
