package com.kawz.tattoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
@ConfigurationPropertiesScan
public class KawzTattooApplication {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public static void main(String[] args) {
        SpringApplication.run(KawzTattooApplication.class, args);
    }
}
