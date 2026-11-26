package com.kawz.tattoo.infrastructure.integration;

import com.kawz.tattoo.domain.exception.IntegracaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Agenda das sessões no Nylas (API v3, chamada direta por REST, sem SDK).
 * Sem NYLAS_API_KEY/NYLAS_GRANT_ID a integração fica desligada: as sessões são salvas normalmente, sem evento.
 */
@Component
public class NylasCalendarClient {
    private static final Logger log = LoggerFactory.getLogger(NylasCalendarClient.class);

    private final RestClient http;
    private final String apiKey;
    private final String grantId;
    private final String calendarId;
    private final ZoneId fuso;
    private final long duracaoHoras;

    public NylasCalendarClient(
            @Value("${kawz.integracoes.nylas.base-url:${nylas.base-url:https://api.us.nylas.com}}") String baseUrl,
            @Value("${kawz.integracoes.nylas.api-key:${nylas.api-key:}}") String apiKey,
            @Value("${kawz.integracoes.nylas.grant-id:${nylas.grant-id:}}") String grantId,
            @Value("${kawz.integracoes.nylas.calendar-id:${nylas.calendar-id:primary}}") String calendarId,
            @Value("${kawz.integracoes.nylas.fuso:${nylas.fuso:America/Sao_Paulo}}") String fuso,
            @Value("${kawz.integracoes.nylas.duracao-horas:${nylas.duracao-sessao-horas:3}}") long duracaoHoras) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(15_000);
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.apiKey = apiKey;
        this.grantId = grantId;
        this.calendarId = calendarId;
        this.fuso = ZoneId.of(fuso);
        this.duracaoHoras = duracaoHoras;
    }

    public boolean ativo() { return !apiKey.isBlank() && !grantId.isBlank(); }

    /** Cria o evento e devolve o id dele no Nylas (null se a integração estiver desligada). */
    public String criarEvento(String titulo, String descricao, String local, LocalDateTime inicio) {
        return criarEvento(titulo, descricao, local, inicio, null);
    }

    public String criarEvento(String titulo, String descricao, String local, LocalDateTime inicio, Integer duracaoHoras) {
        if (!ativo()) return null;
        try {
            Map<?, ?> r = comRetry(() -> http.post()
                    .uri(u -> u.path("/v3/grants/{grant}/events").queryParam("calendar_id", calendarId).build(grantId))
                    .header("Authorization", "Bearer " + apiKey).contentType(MediaType.APPLICATION_JSON)
                    .body(corpo(titulo, descricao, local, inicio, duracaoHoras)).retrieve().body(Map.class));
            Object data = r == null ? null : r.get("data");
            Object id = data instanceof Map<?, ?> m ? m.get("id") : null;
            if (id == null) throw new IntegracaoException("O Nylas não devolveu o id do evento");
            return String.valueOf(id);
        } catch (RestClientException ex) {
            throw new IntegracaoException("Falha ao criar o evento no Nylas", ex);
        }
    }

    public void atualizarEvento(String eventoId, String titulo, String descricao, String local, LocalDateTime inicio) {
        atualizarEvento(eventoId, titulo, descricao, local, inicio, null);
    }

    public void atualizarEvento(String eventoId, String titulo, String descricao, String local, LocalDateTime inicio, Integer duracaoHoras) {
        if (!ativo() || eventoId == null) return;
        try {
            comRetry(() -> http.put()
                    .uri(u -> u.path("/v3/grants/{grant}/events/{id}").queryParam("calendar_id", calendarId)
                            .build(grantId, eventoId))
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo(titulo, descricao, local, inicio, duracaoHoras))
                    .retrieve()
                    .toBodilessEntity());
        } catch (RestClientException ex) {
            throw new IntegracaoException("Falha ao atualizar o evento no Nylas", ex);
        }
    }

    /** "Melhor esforço": a sessão já foi apagada no banco, então só registramos o erro. */
    public void removerEvento(String eventoId) {
        if (!ativo() || eventoId == null) return;
        try {
            http.delete()
                    .uri(u -> u.path("/v3/grants/{grant}/events/{id}").queryParam("calendar_id", calendarId)
                            .build(grantId, eventoId))
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Não foi possível remover o evento {} do Nylas: {}", eventoId, ex.getMessage());
        }
    }

    private Map<String, Object> corpo(String titulo, String descricao, String local, LocalDateTime inicio, Integer duracaoEspecifica) {
        long ini = inicio.atZone(fuso).toEpochSecond();
        Map<String, Object> when = new LinkedHashMap<>();
        when.put("start_time", ini);
        long horas = duracaoEspecifica == null ? duracaoHoras : duracaoEspecifica;
        when.put("end_time", ini + horas * 3600);
        when.put("start_timezone", fuso.getId());
        when.put("end_timezone", fuso.getId());
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("title", titulo);
        if (descricao != null) corpo.put("description", descricao);
        if (local != null) corpo.put("location", local);
        corpo.put("when", when);
        return corpo;
    }

    private <T> T comRetry(Supplier<T> operacao) {
        RestClientException ultima = null;
        for (int tentativa = 0; tentativa < 3; tentativa++) {
            try { return operacao.get(); }
            catch (RestClientException ex) {
                ultima = ex;
                if (tentativa < 2) {
                    try { Thread.sleep(200L * (1L << tentativa)); }
                    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw ex; }
                }
            }
        }
        throw ultima;
    }
}
