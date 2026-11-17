package com.kawz.tattoo.api.orcamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrcamentoInput(
            @NotNull Integer projetoId,
            @NotNull Integer clienteId,
            @Size(max = 150) String nome,
            LocalDate data,
            @NotBlank @Size(max = 45) String status,
            @NotNull @DecimalMin("0.00") BigDecimal total,
            @NotNull @DecimalMin("0.00") BigDecimal deposito,
            @NotNull @Min(1) Integer sessoesEstimadas,
            @Size(max = 45) String formaPagamento,
            LocalDate validade,
            Boolean propostaAceita) {}
