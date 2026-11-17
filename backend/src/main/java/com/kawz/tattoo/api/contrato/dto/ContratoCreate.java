package com.kawz.tattoo.api.contrato.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContratoCreate(@NotNull Integer projetoId,
                                 @Size(max = 45) String limiteAlteracoesArte,
                                 @Size(max = 45) String toleranciaAtraso,
                                 @Size(max = 45) String usoImagem) {}
