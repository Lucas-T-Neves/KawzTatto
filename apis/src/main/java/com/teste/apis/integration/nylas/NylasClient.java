package com.teste.apis.integration.nylas;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.teste.apis.agenda.dto.ParticipanteRequestDTO;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.exception.RecursoNaoEncontradoException;
import com.teste.apis.integration.apoio.ErroProvedor;
import com.teste.apis.integration.apoio.ProvedorHttp;
import com.teste.apis.integration.nylas.dto.NylasEventoRequestDTO;
import com.teste.apis.integration.nylas.dto.NylasEventoResponseDTO;
import com.teste.apis.integration.nylas.dto.NylasParticipanteDTO;
import com.teste.apis.integration.nylas.dto.NylasRespostaDTO;
import com.teste.apis.integration.nylas.dto.NylasWhenDTO;

/**
 * Client do Nylas v3. Concentra URL, autenticacao, conversao de datas para
 * timestamp Unix e o envelope request_id/data.
 *
 * <p>O tratamento de erro (marcar 4xx/5xx, ler corpo, traduzir para
 * IntegrationException) fica no {@link ProvedorHttp} — la, uma vez so.</p>
 */
@Component
public class NylasClient {

    private static final Logger log = LoggerFactory.getLogger(NylasClient.class);
    private static final String PROVEDOR = "Nylas";

    private final RestClient restClient;
    private final NylasProperties props;

    public NylasClient(RestClient nylasRestClient, NylasProperties props) {
        this.restClient = nylasRestClient;
        this.props = props;
    }

    /** Lista eventos no intervalo informado (timestamps Unix em segundos). */
    public List<NylasEventoResponseDTO> listar(Long inicio, Long fim) {
        try {
            NylasRespostaDTO<List<NylasEventoResponseDTO>> resposta = ProvedorHttp.comTratamentoDeErro(
                    restClient.get()
                            .uri(uri -> uri.path("/v3/grants/{grantId}/events")
                                    .queryParam("calendar_id", props.getCalendarId())
                                    .queryParamIfPresent("start", java.util.Optional.ofNullable(inicio))
                                    .queryParamIfPresent("end", java.util.Optional.ofNullable(fim))
                                    .build(Map.of("grantId", props.getGrantId())))
                            .header("Authorization", "Bearer " + props.getApiKey())
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve())
                    .body(new ParameterizedTypeReference<NylasRespostaDTO<List<NylasEventoResponseDTO>>>() { });

            return resposta == null || resposta.data() == null ? List.of() : resposta.data();
        } catch (ErroProvedor e) {
            throw traduzir(e, false);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de rede ao chamar provedor={}", PROVEDOR);
            throw ProvedorHttp.timeout(PROVEDOR, "Tempo esgotado ao consultar a agenda");
        }
    }

    /** Cria um evento e devolve o evento criado. */
    public NylasEventoResponseDTO criar(NylasEventoRequestDTO corpo) {
        try {
            NylasRespostaDTO<NylasEventoResponseDTO> resposta = ProvedorHttp.comTratamentoDeErro(
                    restClient.post()
                            .uri(uri -> uri.path("/v3/grants/{grantId}/events")
                                    .queryParam("calendar_id", props.getCalendarId())
                                    .queryParam("notify_participants", props.isNotifyParticipantes())
                                    .build(Map.of("grantId", props.getGrantId())))
                            .header("Authorization", "Bearer " + props.getApiKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(corpo)
                            .retrieve())
                    .body(new ParameterizedTypeReference<NylasRespostaDTO<NylasEventoResponseDTO>>() { });

            return resposta == null ? null : resposta.data();
        } catch (ErroProvedor e) {
            throw traduzir(e, false);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de rede ao chamar provedor={}", PROVEDOR);
            throw ProvedorHttp.timeout(PROVEDOR, "Tempo esgotado ao criar o evento");
        }
    }

    /** Atualiza um evento. */
    public NylasEventoResponseDTO atualizar(String eventoId, NylasEventoRequestDTO corpo) {
        try {
            NylasRespostaDTO<NylasEventoResponseDTO> resposta = ProvedorHttp.comTratamentoDeErro(
                    restClient.put()
                            .uri(uri -> uri.path("/v3/grants/{grantId}/events/{eventoId}")
                                    .queryParam("calendar_id", props.getCalendarId())
                                    .queryParam("notify_participants", props.isNotifyParticipantes())
                                    .build(Map.of("grantId", props.getGrantId(), "eventoId", eventoId)))
                            .header("Authorization", "Bearer " + props.getApiKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(corpo)
                            .retrieve())
                    .body(new ParameterizedTypeReference<NylasRespostaDTO<NylasEventoResponseDTO>>() { });

            return resposta == null ? null : resposta.data();
        } catch (ErroProvedor e) {
            throw traduzir(e, true);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de rede ao chamar provedor={}", PROVEDOR);
            throw ProvedorHttp.timeout(PROVEDOR, "Tempo esgotado ao atualizar o evento");
        }
    }

    /** Apaga um evento. */
    public void remover(String eventoId) {
        try {
            ProvedorHttp.comTratamentoDeErro(
                    restClient.delete()
                            .uri(uri -> uri.path("/v3/grants/{grantId}/events/{eventoId}")
                                    .queryParam("calendar_id", props.getCalendarId())
                                    .queryParam("notify_participants", props.isNotifyParticipantes())
                                    .build(Map.of("grantId", props.getGrantId(), "eventoId", eventoId)))
                            .header("Authorization", "Bearer " + props.getApiKey())
                            .retrieve())
                    .toBodilessEntity();
        } catch (ErroProvedor e) {
            throw traduzir(e, true);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de rede ao chamar provedor={}", PROVEDOR);
            throw ProvedorHttp.timeout(PROVEDOR, "Tempo esgotado ao apagar o evento");
        }
    }

    /** Monta o corpo no formato do Nylas a partir dos dados do dominio. */
    public NylasEventoRequestDTO montarCorpo(com.teste.apis.agenda.dto.EventoRequestDTO request,
                                             String fusoHorario) {
        // lista vazia (nunca null): null seria lido pelo Nylas como "nao alterar",
        // impedindo remover participantes em um PUT
        List<NylasParticipanteDTO> participantes = request.participantes() == null
                ? List.of()
                : request.participantes().stream()
                        .map(p -> new NylasParticipanteDTO(p.nome(), p.email()))
                        .toList();

        return new NylasEventoRequestDTO(
                request.titulo(),
                new NylasWhenDTO(
                        request.inicio().toEpochSecond(),
                        request.fim().toEpochSecond(),
                        fusoHorario,
                        fusoHorario),
                participantes,
                request.local(),
                request.descricao(),
                Boolean.TRUE);
    }

    /**
     * Regra propria do Nylas: sem tabela propria, o 404 dele em chamada sobre
     * um evento especifico significa "nao existe" para o cliente (404 da API).
     * Nas demais (listar, criar), 404 aponta problema de configuracao e vira 502.
     * O resto da traducao e comum a todo client e esta no ProvedorHttp.
     */
    private IntegrationException traduzir(ErroProvedor erro, boolean itemEspecifico) {
        if (erro.getStatus() == 404 && itemEspecifico) {
            log.warn("Recurso nao encontrado no provedor={}", PROVEDOR);
            throw new RecursoNaoEncontradoException("Evento nao encontrado");
        }
        return ProvedorHttp.traduzir(erro, PROVEDOR);
    }
}
