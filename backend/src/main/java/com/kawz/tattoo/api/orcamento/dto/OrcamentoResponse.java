package com.kawz.tattoo.api.orcamento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrcamentoResponse(Integer id, Integer projetoId, Integer clienteId, String nome,
                                    LocalDate data, String status, BigDecimal total, BigDecimal deposito,
                                    Integer sessoesEstimadas, BigDecimal valorSessaoEstimado,
                                    String formaPagamento, LocalDate validade, Boolean propostaAceita) {}
