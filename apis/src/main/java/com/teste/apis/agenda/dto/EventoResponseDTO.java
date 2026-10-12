package com.teste.apis.agenda.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Evento devolvido pela API. O Nylas e a fonte da verdade: nao ha entity. */
public record EventoResponseDTO(
        String id,
        String titulo,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        String fusoHorario,
        String descricao,
        String local,
        List<ParticipanteResponseDTO> participantes
) {

    public record ParticipanteResponseDTO(String nome, String email) {
    }
}
