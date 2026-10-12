package com.teste.apis.integration.nylas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.teste.apis.agenda.dto.EventoRequestDTO;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.exception.RecursoNaoEncontradoException;
import com.teste.apis.integration.nylas.dto.NylasEventoRequestDTO;
import com.teste.apis.integration.nylas.dto.NylasEventoResponseDTO;
import com.teste.apis.integration.nylas.dto.NylasParticipanteDTO;
import com.teste.apis.integration.nylas.dto.NylasWhenDTO;

/** Testes do client do Nylas com MockRestServiceServer. */
class NylasClientTest {

    private static final String BASE_URL = "http://localhost:8080/nylas";
    private static final String GRANT = "grant-fake-de-teste";

    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private NylasClient client;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new NylasClient(builder.build(), props());
    }

    private NylasProperties props() {
        NylasProperties p = new NylasProperties();
        p.setBaseUrl(BASE_URL);
        p.setApiKey("chave-fake");
        p.setGrantId(GRANT);
        p.setCalendarId("primary");
        p.setNotifyParticipantes(false);
        return p;
    }

    private EventoRequestDTO request() {
        return new EventoRequestDTO(
                "Reuniao com Bruno",
                OffsetDateTime.parse("2026-09-19T10:00:00-03:00"),
                OffsetDateTime.parse("2026-09-19T11:30:00-03:00"),
                "America/Sao_Paulo",
                "Fechar orcamento",
                "Estudio",
                List.of(new com.teste.apis.agenda.dto.ParticipanteRequestDTO("Bruno", "bruno@exemplo.com")));
    }

    @Test
    @DisplayName("deve converter datas para timestamp Unix e fuso ao criar evento")
    void criaEvento() {
        long inicioEsperado = OffsetDateTime.parse("2026-09-19T10:00:00-03:00").toEpochSecond();
        long fimEsperado = OffsetDateTime.parse("2026-09-19T11:30:00-03:00").toEpochSecond();

        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events?calendar_id=primary&notify_participants=false"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer chave-fake"))
                .andExpect(jsonPath("$.title").value("Reuniao com Bruno"))
                .andExpect(jsonPath("$.when.start_time").value(inicioEsperado))
                .andExpect(jsonPath("$.when.end_time").value(fimEsperado))
                .andExpect(jsonPath("$.when.start_timezone").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.participants[0].email").value("bruno@exemplo.com"))
                .andRespond(withSuccess("""
                        {
                          "request_id": "abc",
                          "data": {
                            "id": "evt-1",
                            "title": "Reuniao com Bruno",
                            "when": { "start_time": %d, "end_time": %d,
                                      "start_timezone": "America/Sao_Paulo",
                                      "end_timezone": "America/Sao_Paulo" },
                            "participants": [ { "name": "Bruno", "email": "bruno@exemplo.com" } ]
                          }
                        }
                        """.formatted(inicioEsperado, fimEsperado), MediaType.APPLICATION_JSON));

        NylasEventoResponseDTO resposta = client.criar(client.montarCorpo(request(), "America/Sao_Paulo"));

        server.verify();
        assertThat(resposta.id()).isEqualTo("evt-1");
        assertThat(resposta.participantes().get(0).email()).isEqualTo("bruno@exemplo.com");
    }

    @Test
    @DisplayName("deve permitir participante sem nome e usar o fuso America/Sao_Paulo")
    void participanteSemNome() {
        EventoRequestDTO semNome = new EventoRequestDTO(
                "Sessao",
                OffsetDateTime.parse("2026-09-19T10:00:00-03:00"),
                OffsetDateTime.parse("2026-09-19T11:00:00-03:00"),
                null,
                null,
                null,
                List.of(new com.teste.apis.agenda.dto.ParticipanteRequestDTO(null, "so.email@exemplo.com")));

        NylasEventoRequestDTO corpo = client.montarCorpo(semNome, "America/Sao_Paulo");

        assertThat(corpo.participantes().get(0).nome()).isNull();
        assertThat(corpo.participantes().get(0).email()).isEqualTo("so.email@exemplo.com");
        assertThat(corpo.when().startTimezone()).isEqualTo("America/Sao_Paulo");
        assertThat(corpo.busy()).isTrue();
    }

    @Test
    @DisplayName("deve enviar lista vazia de participantes (nunca null) para permitir limpar")
    void participantesVazio() {
        EventoRequestDTO semParticipantes = new EventoRequestDTO(
                "Sessao",
                OffsetDateTime.parse("2026-09-19T10:00:00-03:00"),
                OffsetDateTime.parse("2026-09-19T11:00:00-03:00"),
                null,
                null,
                null,
                List.of());

        NylasEventoRequestDTO corpo = client.montarCorpo(semParticipantes, "America/Sao_Paulo");
        assertThat(corpo.participantes()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("deve enviar lista vazia tambem quando participantes vier null")
    void participantesNull() {
        EventoRequestDTO nulo = new EventoRequestDTO(
                "Sessao",
                OffsetDateTime.parse("2026-09-19T10:00:00-03:00"),
                OffsetDateTime.parse("2026-09-19T11:00:00-03:00"),
                null,
                null,
                null,
                null);

        NylasEventoRequestDTO corpo = client.montarCorpo(nulo, "America/Sao_Paulo");
        assertThat(corpo.participantes()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("deve enviar start e end como timestamp na listagem filtrada")
    void listaComFiltro() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events?calendar_id=primary&start=1797754800&end=1797760200"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer chave-fake"))
                .andRespond(withSuccess("""
                        {
                          "request_id": "xyz",
                          "data": [
                            { "id": "evt-1", "title": "Reuniao", "when": { "start_time": 1797754800 } }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<NylasEventoResponseDTO> eventos = client.listar(1797754800L, 1797760200L);

        server.verify();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).id()).isEqualTo("evt-1");
    }

    @Test
    @DisplayName("deve devolver lista vazia quando o provedor nao tem eventos")
    void listaVazia() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events?calendar_id=primary"))
                .andRespond(withSuccess("""
                        { "request_id": "xyz", "data": [] }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.listar(null, null)).isEmpty();
    }

    @Test
    @DisplayName("deve tratar erro 4xx como falha de integracao")
    void erroCliente() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events?calendar_id=primary"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                { "request_id": "xyz", "error": { "message": "calendario invalido" } }
                                """));

        assertThatThrownBy(() -> client.listar(null, null))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("calendario invalido");
    }

    @Test
    @DisplayName("deve traduzir 404 do provedor para recurso nao encontrado")
    void eventoInexistente() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events/evt-999?calendar_id=primary&notify_participants=false"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                { "request_id": "xyz", "error": { "message": "evento nao existe" } }
                                """));

        // sem tabela propria, 404 do Nylas significa inexistente para o cliente
        assertThatThrownBy(() -> client.remover("evt-999"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("deve tratar 404 da listagem como falha de integracao, nao como inexistente")
    void inexistenteSomenteParaEvento() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events?calendar_id=primary"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                { "request_id": "xyz", "error": { "message": "grant nao existe" } }
                                """));

        // 404 na listagem aponta problema de configuracao (grant/calendario),
        // nao evento inexistente: por isso segue como falha de integracao.
        assertThatThrownBy(() -> client.listar(null, null))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("grant nao existe");
    }

    @Test
    @DisplayName("deve sinalizar timeout em falha de rede")
    void falhaDeRede() {
        RestClient semServidor = RestClient.builder().baseUrl("http://127.0.0.1:1").build();
        NylasClient clientSemServidor = new NylasClient(semServidor, props());

        assertThatThrownBy(() -> clientSemServidor.listar(null, null))
                .isInstanceOf(IntegrationException.class)
                .satisfies(e -> assertThat(((IntegrationException) e).isTimeout()).isTrue());
    }

    @Test
    @DisplayName("deve usar PUT com o id do evento ao atualizar")
    void atualizaEvento() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events/evt-1?calendar_id=primary&notify_participants=false"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.title").value("Novo titulo"))
                .andRespond(withSuccess("""
                        { "request_id": "abc", "data": { "id": "evt-1", "title": "Novo titulo" } }
                        """, MediaType.APPLICATION_JSON));

        NylasEventoRequestDTO corpo = new NylasEventoRequestDTO(
                "Novo titulo",
                new NylasWhenDTO(1797754800L, 1797760200L, "America/Sao_Paulo", "America/Sao_Paulo"),
                List.of(new NylasParticipanteDTO("Bruno", "bruno@exemplo.com")),
                null,
                null,
                Boolean.TRUE);

        NylasEventoResponseDTO resposta = client.atualizar("evt-1", corpo);

        server.verify();
        assertThat(resposta.titulo()).isEqualTo("Novo titulo");
    }

    @Test
    @DisplayName("deve usar DELETE com o id do evento ao apagar")
    void apagaEvento() {
        server.expect(requestTo(BASE_URL + "/v3/grants/" + GRANT
                        + "/events/evt-1?calendar_id=primary&notify_participants=false"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess("""
                        { "request_id": "abc" }
                        """, MediaType.APPLICATION_JSON));

        client.remover("evt-1");

        server.verify();
    }
}
