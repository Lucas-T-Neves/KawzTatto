package com.teste.apis.integration.apitemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resposta do APITemplate.io para a criacao do PDF.
 * Ausencia de downloadUrl e tratada como falha pelo client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTemplatePdfResponse(

        @JsonProperty("status")
        String status,

        @JsonProperty("download_url")
        String downloadUrl,

        @JsonProperty("transaction_ref")
        String transactionRef,

        @JsonProperty("template_id")
        String templateId,

        @JsonProperty("message")
        String mensagem
) {

    public boolean sucesso() {
        return "success".equalsIgnoreCase(status) && downloadUrl != null && !downloadUrl.isBlank();
    }
}
