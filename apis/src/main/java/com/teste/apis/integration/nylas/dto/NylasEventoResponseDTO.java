package com.teste.apis.integration.nylas.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Evento como o Nylas devolve. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NylasEventoResponseDTO(

        @JsonProperty("id")
        String id,

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

        @JsonProperty("calendar_id")
        String calendarId,

        @JsonProperty("status")
        String status
) {
}
