package com.teste.apis.agenda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Participante de um evento. O nome e opcional. */
public record ParticipanteRequestDTO(

        @Size(max = 150, message = "nome do participante deve ter no maximo 150 caracteres")
        String nome,

        @NotBlank(message = "e-mail do participante e obrigatorio")
        @jakarta.validation.constraints.Email(message = "e-mail do participante invalido")
        String email
) {
}
