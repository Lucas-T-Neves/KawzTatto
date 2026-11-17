package com.kawz.tattoo.api.contrato.dto;

import jakarta.validation.constraints.Size;

public record ContratoInput(
            @Size(max = 45) String limiteAlteracoesArte,
            @Size(max = 45) String toleranciaAtraso,
            @Size(max = 45) String usoImagem) {}
