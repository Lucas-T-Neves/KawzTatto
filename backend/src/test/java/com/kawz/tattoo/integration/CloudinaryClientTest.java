package com.kawz.tattoo.integration;

import com.kawz.tattoo.domain.exception.IntegracaoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O SDK do Cloudinary fala direto com api.cloudinary.com, então o upload real não roda aqui
 * (sem rede externa nos testes). Cobrimos o comportamento sem configuração; o fluxo com upload
 * é coberto em EndpointsApiTest com o CloudinaryClient simulado.
 */
class CloudinaryClientTest {

    @Test
    void semChavesFicaNaoConfiguradoEUploadFalhaComIntegracaoException() {
        var client = new CloudinaryClient("", "", "", "kawz-tattoo");

        assertThat(client.configurado()).isFalse();
        assertThatThrownBy(() -> client.enviar(new byte[]{1, 2, 3}, 1))
                .isInstanceOf(IntegracaoException.class);
    }

    @Test
    void removerSemConfiguracaoOuSemPublicIdNaoFazNada() {
        var client = new CloudinaryClient("", "", "", "kawz-tattoo");

        assertThatCode(() -> client.remover("qualquer")).doesNotThrowAnyException();
        assertThatCode(() -> client.remover(null)).doesNotThrowAnyException();
    }

    @Test
    void comTresChavesFicaConfigurado() {
        var client = new CloudinaryClient("nuvem", "chave", "segredo", "kawz-tattoo");

        assertThat(client.configurado()).isTrue();
    }
}
