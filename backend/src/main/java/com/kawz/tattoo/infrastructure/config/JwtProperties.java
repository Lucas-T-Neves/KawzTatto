package com.kawz.tattoo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * Configuração do JWT ({@code kawz.jwt.*}).
 *
 * <p>A validação acontece na construção do bean: se o segredo estiver ausente ou
 * tiver menos de 32 bytes, a aplicação não sobe. Em produção o valor vem de
 * {@code JWT_SECRET} sem valor padrão (ver {@code application-prod.yml}), portanto
 * um deploy sem segredo falha imediatamente em vez de usar um segredo conhecido.</p>
 */
@ConfigurationProperties(prefix = "kawz.jwt")
public record  JwtProperties(String segredo, long validadeMinutos) {

    private static final int MINIMO_BYTES = 32;

    public JwtProperties {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException(
                    "kawz.jwt.segredo não configurado: defina a variável de ambiente JWT_SECRET");
        }
        if (segredo.getBytes(StandardCharsets.UTF_8).length < MINIMO_BYTES) {
            throw new IllegalStateException(
                    "kawz.jwt.segredo precisa ter ao menos " + MINIMO_BYTES + " bytes (JWT_SECRET)");
        }
        if (validadeMinutos <= 0) {
            throw new IllegalStateException("kawz.jwt.validade-minutos deve ser maior que zero");
        }
    }
}
