package com.kawz.tattoo.api.followup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record FollowUpInput(@NotNull Integer clienteId,
                                @NotBlank @Size(max = 45) String tipoContato,
                                @NotBlank @Size(max = 45) String statusContato,
                                @NotNull LocalDate dataPrevista,
                                LocalDate dataRealizacao,
                                @Size(max = 10000) String descricaoObservacao,
                                @Size(max = 45) String proximaAcao) {}
