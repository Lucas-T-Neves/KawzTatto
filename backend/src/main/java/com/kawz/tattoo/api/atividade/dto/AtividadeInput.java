package com.kawz.tattoo.api.atividade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record AtividadeInput(@NotNull Integer tatuadorId,
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
        public AtividadeInput(Integer tatuadorId, String descricao, String tipo) {
            this(tatuadorId, null, null, descricao, tipo, null, descricao,
                    null, null, null, null, null);
        }
    }
