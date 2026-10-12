package com.teste.apis.integration.nylas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Participante no formato do Nylas. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NylasParticipanteDTO(

        @JsonProperty("name")
        String nome,

        @JsonProperty("email")
        String email
) {
}
