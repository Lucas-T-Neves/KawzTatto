package com.kawz.tattoo.integration;

import com.kawz.tattoo.domain.exception.IntegracaoException;
import com.kawz.tattoo.support.FakeServer;
import com.kawz.tattoo.support.FakeServer.Resp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testa o NylasCalendarClient contra um servidor HTTP falso (sem rede externa). */
class NylasCalendarClientTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 1, 15, 14, 0);
    private FakeServer server;

    @BeforeEach
    void subir() throws Exception {
        server = new FakeServer(req -> new Resp(200, "{\"request_id\":\"1\",\"data\":{\"id\":\"evt-123\"}}"));
    }

    @AfterEach
    void descer() { server.close(); }

    private NylasCalendarClient ativo() {
        return new NylasCalendarClient(server.url(), "chave-teste", "grant-1", "primary", "America/Sao_Paulo", 3);
    }

    @Test
    void semChaveOuGrantFicaDesligadoENaoChamaAApi() {
        var desligado = new NylasCalendarClient(server.url(), "", "", "primary", "America/Sao_Paulo", 3);

        assertThat(desligado.ativo()).isFalse();
        assertThat(desligado.criarEvento("t", "d", "l", INICIO)).isNull();
        desligado.atualizarEvento("evt", "t", "d", "l", INICIO);
        desligado.removerEvento("evt");
        assertThat(server.requests).isEmpty();
    }

    @Test
    void criaEventoComHorarioDuracaoEDevolveOId() {
        String id = ativo().criarEvento("Sessão - Ana", "Fechamento", "Estúdio", INICIO);

        assertThat(id).isEqualTo("evt-123");
        var req = server.ultima();
        assertThat(req.method()).isEqualTo("POST");
        assertThat(req.uri()).isEqualTo("/v3/grants/grant-1/events?calendar_id=primary");
        assertThat(req.headers().get("authorization")).isEqualTo("Bearer chave-teste");
        long inicio = INICIO.atZone(ZoneId.of("America/Sao_Paulo")).toEpochSecond();
        assertThat(req.body())
                .contains("\"title\":\"Sessão - Ana\"")
                .contains("\"location\":\"Estúdio\"")
                .contains("\"start_time\":" + inicio)
                .contains("\"end_time\":" + (inicio + 3 * 3600))
                .contains("America/Sao_Paulo");
    }

    @Test
    void atualizaEventoExistente() {
        ativo().atualizarEvento("evt-123", "Sessão - Ana", null, "Estúdio", INICIO.plusDays(1));

        var req = server.ultima();
        assertThat(req.method()).isEqualTo("PUT");
        assertThat(req.uri()).isEqualTo("/v3/grants/grant-1/events/evt-123?calendar_id=primary");
    }

    @Test
    void removeEvento() {
        ativo().removerEvento("evt-123");

        var req = server.ultima();
        assertThat(req.method()).isEqualTo("DELETE");
        assertThat(req.uri()).isEqualTo("/v3/grants/grant-1/events/evt-123?calendar_id=primary");
    }

    @Test
    void falhaAoCriarOuAtualizarViraIntegracaoException() {
        server.responder(req -> new Resp(500, "{}"));

        assertThatThrownBy(() -> ativo().criarEvento("t", null, null, INICIO))
                .isInstanceOf(IntegracaoException.class);
        assertThatThrownBy(() -> ativo().atualizarEvento("evt", "t", null, null, INICIO))
                .isInstanceOf(IntegracaoException.class);
    }

    @Test
    void falhaAoRemoverNaoPropagaErro() {
        server.responder(req -> new Resp(500, "{}"));

        assertThatCode(() -> ativo().removerEvento("evt-123")).doesNotThrowAnyException();
    }

    @Test
    void respostaSemIdViraIntegracaoException() {
        server.responder(req -> new Resp(200, "{\"data\":{}}"));

        assertThatThrownBy(() -> ativo().criarEvento("t", null, null, INICIO))
                .isInstanceOf(IntegracaoException.class);
    }
}
