package com.kawz.tattoo.api.movimentacao.dto;

import java.math.BigDecimal;

public record MovimentacaoResumoResponse(String mes, BigDecimal receita, BigDecimal despesas,
                                               BigDecimal saldo, BigDecimal pendencias,
                                               BigDecimal receitasPendentes, BigDecimal despesasPendentes) {}
