package com.kawz.tattoo.api.movimentacao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoInput(
            @NotNull Integer projetoId,
            @NotBlank @Size(max = 45) @Pattern(regexp = "ENTRADA|SAIDA",
                    message = "deve ser ENTRADA ou SAIDA") String naturezaTransacao,
            @NotBlank @Size(max = 45) String categoria,
            LocalDate dataOcorrencia,
            @NotNull @DecimalMin("0.00") BigDecimal valorMonetario,
            @Size(max = 45) String formaPagamento,
            @NotBlank @Size(max = 45) @Pattern(regexp = "(?i)pago|pendente|cancelado",
                    message = "deve ser pago, pendente ou cancelado") String statusPagamento) {}
