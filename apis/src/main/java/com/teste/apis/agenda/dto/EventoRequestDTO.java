package com.teste.apis.agenda.dto;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados de criacao/atualizacao de um evento da agenda. */
public record EventoRequestDTO(

        @NotBlank(message = "titulo e obrigatorio")
        @Size(max = 200, message = "titulo deve ter no maximo 200 caracteres")
        String titulo,

        @NotNull(message = "inicio e obrigatorio")
        OffsetDateTime inicio,

        @NotNull(message = "fim e obrigatorio")
        OffsetDateTime fim,

        @Size(max = 60, message = "fuso horario invalido")
        String fusoHorario,

        @Size(max = 1000, message = "descricao deve ter no maximo 1000 caracteres")
        String descricao,

        @Size(max = 200, message = "local deve ter no maximo 200 caracteres")
        String local,

        @Valid
        List<ParticipanteRequestDTO> participantes
) {

    @AssertTrue(message = "fim deve ser depois de inicio")
    public boolean isPeriodoValido() {
        if (inicio == null || fim == null) {
            return true;
        }
        return fim.isAfter(inicio);
    }
}
