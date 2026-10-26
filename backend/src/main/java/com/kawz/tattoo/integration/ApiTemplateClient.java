package com.kawz.tattoo.integration;

import com.kawz.tattoo.domain.exception.IntegracaoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** Gera o PDF do contrato no APITemplate.io (POST /v2/create-pdf?template_id=...). */
@Component
public class ApiTemplateClient {

    public record PdfGerado(String downloadUrl, String transactionRef) {}

    private final RestClient http;
    private final String apiKey;
    private final String templateId;

    public ApiTemplateClient(
            @Value("${apitemplate.base-url:https://rest.apitemplate.io}") String baseUrl,
            @Value("${apitemplate.api-key:}") String apiKey,
            @Value("${apitemplate.template-contrato-id:}") String templateId) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(30_000);
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.apiKey = apiKey;
        this.templateId = templateId;
    }

    /** @param dados variáveis do template; as chaves precisam bater com os placeholders cadastrados no APITemplate */
    public PdfGerado gerarContrato(Map<String, Object> dados) {
        if (apiKey.isBlank() || templateId.isBlank()) {
            throw new IntegracaoException("APITemplate não configurado (defina APITEMPLATE_API_KEY e APITEMPLATE_TEMPLATE_ID)");
        }
        Map<?, ?> resposta;
        try {
            resposta = http.post()
                    .uri(u -> u.path("/v2/create-pdf").queryParam("template_id", templateId).build())
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dados)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException ex) {
            throw new IntegracaoException("Falha ao chamar o APITemplate", ex);
        }
        if (resposta == null || !"success".equals(resposta.get("status")) || resposta.get("download_url") == null) {
            throw new IntegracaoException("O APITemplate não devolveu um PDF");
        }
        Object ref = resposta.get("transaction_ref");
        return new PdfGerado(String.valueOf(resposta.get("download_url")), ref == null ? null : String.valueOf(ref));
    }
}
