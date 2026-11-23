package com.kawz.tattoo.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Garante o "fail fast" da configuração de JWT: sem segredo, com segredo curto
 * ou com validade inválida a aplicação não deve subir.
 */
class JwtPropertiesTest {

    @Test
    @DisplayName("aceita segredo com 32 bytes ou mais")
    void aceitaSegredoValido() {
        assertThatCode(() -> new JwtProperties("x".repeat(32), 120)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejeita segredo ausente, em branco ou curto")
    void rejeitaSegredoInvalido() {
        assertThatThrownBy(() -> new JwtProperties(null, 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtProperties("   ", 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtProperties("curto", 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    @DisplayName("rejeita validade não positiva")
    void rejeitaValidadeInvalida() {
        assertThatThrownBy(() -> new JwtProperties("x".repeat(32), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("validade-minutos");
    }
}
