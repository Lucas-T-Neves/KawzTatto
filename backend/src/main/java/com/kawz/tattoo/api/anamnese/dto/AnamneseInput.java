package com.kawz.tattoo.api.anamnese.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnamneseInput(@NotNull Integer clienteId,
                                @Size(max = 45) String dadosSaude,
                                @Size(max = 45) String preparacaoTatuagem) {}
