package com.kawz.tattoo.api.contrato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoViewResponse(Integer contratoId, String nomeCliente, String telefoneCliente,
                                       String cpfCliente, String nomeArtista, String descricaoTatuagem, String localCorpo,
                                       LocalDate dataInicio, LocalDate dataFim, BigDecimal valorOrcado,
                                       Boolean sinalPago, String formaPagamento, String limiteAlteracoesArte,
                                       String toleranciaAtraso, String usoImagem, LocalDate dataAssinatura,
                                       String statusContrato, String pdfUrl) {}
