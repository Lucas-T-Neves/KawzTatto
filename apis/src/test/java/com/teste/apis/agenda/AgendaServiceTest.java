package com.teste.apis.agenda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teste.apis.agenda.dto.EventoRequestDTO;
import com.teste.apis.agenda.dto.EventoResponseDTO;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.integration.nylas.NylasClient;
import com.teste.apis.integration.nylas.dto.NylasEventoRequestDTO;
import com.teste.apis.integration.nylas.dto.NylasEventoResponseDTO;
import com.teste.apis.integration.nylas.dto.NylasWhenDTO;

/** Testes do fluxo da agenda com o client do Nylas mockado. */
@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    private static final OffsetDateTime INICIO =
            OffsetDateTime.parse("2026-09-19T10:00:00-03:00");
    private static final OffsetDateTime FIM =
            OffsetDateTime.parse("2026-09-19T11:30:00-03:00");

    private static final EventoRequestDTO REQUEST = new EventoRequestDTO(
            "Reuniao com Bruno", INICIO, FIM, "America/Sao_Paulo",
            "Fechar orcamento", "Estudio", List.of());

    @Mock
    private NylasClient nylasClient;

    @InjectMocks
    private AgendaService service;

    @Test
    @DisplayName("deve recusar intervalo com fim anterior ou igual ao inicio")
    void periodoInvalidoNaListagem() {
        assertThatThrownBy(() -> service.listar(INICIO, INICIO.minusHours(1)))
                .isInstanceOf(com.teste.apis.exception.RegraNegocioException.class)
                .hasMessageContaining("fim deve ser depois de inicio");

        assertThatThrownBy(() -> service.listar(INICIO, INICIO))
                .isInstanceOf(com.teste.apis.exception.RegraNegocioException.class)
                .hasMessageContaining("fim deve ser depois de inicio");

        verify(nylasClient, never()).listar(any(), any());
    }

    @Test
    @DisplayName("deve listar eventos convertendo a resposta do provedor")
    void listaEventos() {
        NylasEventoResponseDTO evento = new NylasEventoResponseDTO(
                "evt-1", "Reuniao",
                new NylasWhenDTO(INICIO.toEpochSecond(), FIM.toEpochSecond(),
                        "America/Sao_Paulo", "America/Sao_Paulo"),
                null, null, null, "primary", "confirmed");

        when(nylasClient.listar(INICIO.toEpochSecond(), FIM.toEpochSecond()))
                .thenReturn(List.of(evento));

        List<EventoResponseDTO> resposta = service.listar(INICIO, FIM);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).id()).isEqualTo("evt-1");
        assertThat(resposta.get(0).inicio()).isEqualTo(INICIO);
        assertThat(resposta.get(0).fim()).isEqualTo(FIM);
    }

    @Test
    @DisplayName("deve criar evento e devolver os dados no formato do dominio")
    void criaEvento() {
        NylasEventoResponseDTO criado = new NylasEventoResponseDTO(
                "evt-1", "Reuniao com Bruno",
                new NylasWhenDTO(INICIO.toEpochSecond(), FIM.toEpochSecond(),
                        "America/Sao_Paulo", "America/Sao_Paulo"),
                null, null, "Fechar orcamento", "primary", "confirmed");

        when(nylasClient.montarCorpo(any(), any())).thenReturn(corpoFake());
        when(nylasClient.criar(any())).thenReturn(criado);

        EventoResponseDTO resposta = service.criar(REQUEST);

        assertThat(resposta.id()).isEqualTo("evt-1");
        assertThat(resposta.fusoHorario()).isEqualTo("America/Sao_Paulo");
        assertThat(resposta.inicio()).isEqualTo(INICIO);
    }

    @Test
    @DisplayName("deve devolver falha de integracao quando o provedor nao devolve o evento criado")
    void provedorSemResposta() {
        when(nylasClient.montarCorpo(any(), any())).thenReturn(corpoFake());
        when(nylasClient.criar(any())).thenReturn(null);

        assertThatThrownBy(() -> service.criar(REQUEST))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("nao devolveu");
    }

    private NylasEventoRequestDTO corpoFake() {
        return new NylasEventoRequestDTO(
                "Reuniao com Bruno",
                new NylasWhenDTO(INICIO.toEpochSecond(), FIM.toEpochSecond(),
                        "America/Sao_Paulo", "America/Sao_Paulo"),
                List.of(), null, "Fechar orcamento", Boolean.TRUE);
    }
}
