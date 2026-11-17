package com.kawz.tattoo.api.movimentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoResponse(Integer id, Integer projetoId, String naturezaTransacao,
                                       String categoria, LocalDate dataOcorrencia, BigDecimal valorMonetario,
                                       String formaPagamento, String statusPagamento) {}
