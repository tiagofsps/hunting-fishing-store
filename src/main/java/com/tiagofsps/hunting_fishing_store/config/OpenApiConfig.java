package com.tiagofsps.hunting_fishing_store.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Metadata shown in Swagger UI and the OpenAPI JSON. The HTTP bearer scheme
 * lets reviewers authenticate from the "Authorize" button with an access token
 * obtained from {@code POST /api/v1/auth/login}.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI storeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hunting & Fishing Store API")
                        .description("""
                                REST API for a hunting and fishing equipment store: catalog, cart,
                                checkout, payments, coupons, promotions and newsletter.

                                Catalog reads are public. Authenticated requests use a JWT access token
                                from `POST /api/v1/auth/login`; admin-only operations require the
                                `ROLE_ADMIN` authority. This repository is a proprietary portfolio
                                project, published for review only.""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tiago Pires")
                                .url("https://github.com/tiagofsps"))
                        .license(new License().name("Proprietary - All Rights Reserved")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
