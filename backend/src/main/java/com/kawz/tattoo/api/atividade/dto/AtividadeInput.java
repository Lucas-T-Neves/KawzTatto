package com.kawz.tattoo.api.atividade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record AtividadeInput(/* tatuadorId enviado pelo frontend: aceito e ignorado (o dono vem do token) */
                              Integer tatuadorId,
                                 Integer projetoId,
                                 Integer clienteId,
                                 @NotBlank @Size(max = 45) String descricao,
                                 @NotBlank @Size(max = 45) String tipo,
                                 @Size(max = 45) String status,
                                 @Size(max = 120) String titulo,
                                 LocalDate data,
                                 LocalTime inicio,
                                  LocalTime termino,
                                  @Size(max = 255) String local,
                                  @Size(max = 2000) String observacoes) {

    }
