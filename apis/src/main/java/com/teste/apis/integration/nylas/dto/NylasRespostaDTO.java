package com.teste.apis.integration.nylas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope da resposta do Nylas: tudo vem dentro de `data`,
 * acompanhado de um `request_id`.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NylasRespostaDTO<T>(

        @JsonProperty("request_id")
        String requestId,

        @JsonProperty("data")
        T data
) {
}
