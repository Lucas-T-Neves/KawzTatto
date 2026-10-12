package com.teste.apis.integration.nylas.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Corpo de criacao/atualizacao de evento no formato do Nylas. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NylasEventoRequestDTO(

        @JsonProperty("title")
        String titulo,

        @JsonProperty("when")
        NylasWhenDTO when,

        @JsonProperty("participants")
        List<NylasParticipanteDTO> participantes,

        @JsonProperty("location")
        String local,

        @JsonProperty("description")
        String descricao,

        @JsonProperty("busy")
        Boolean busy
) {
}
