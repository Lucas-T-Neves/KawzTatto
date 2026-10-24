package com.kawz.tattoo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados do Swagger UI (/swagger-ui.html) e do documento /v3/api-docs. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI kawzOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kawz Tattoo API")
                        .description("""
                                Gestão de tatuadores, clientes, projetos, sessões, movimentações \
                                financeiras e contratos do estúdio (Sprint 2+).

                                Autentique-se em `POST /api/v1/tatuadores/autenticacao` e use o \
                                token em "Authorize" (Bearer) nas demais rotas. Erros seguem \
                                RFC 9457 (Problem Details); listagens aceitam `pagina` e `tamanho`.""")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
