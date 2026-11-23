package com.kawz.tattoo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Aplicação do backend Kawz Tattoo.
 *
 * <p>{@code @ConfigurationPropertiesScan} registra {@code JwtProperties}, que valida
 * o segredo JWT no start (sem segredo válido a aplicação não sobe).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class KawzTattooApplication {

    public static void main(String[] args) {
        SpringApplication.run(KawzTattooApplication.class, args);
    }
}
