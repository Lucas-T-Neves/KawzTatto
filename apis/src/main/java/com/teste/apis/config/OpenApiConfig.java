package com.teste.apis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apisOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("APIs do Estudio")
                .version("v1")
                .description("Contratos em PDF (APITemplate.io), imagens (Cloudinary) e agenda (Nylas)"));
    }
}
