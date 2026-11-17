package com.kawz.tattoo.api.contrato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContratoResponse(Integer id, String limiteAlteracoesArte, String toleranciaAtraso,
                                   String usoImagem, LocalDate dataAssinatura,
                                   String statusContrato, LocalDateTime criadoEm,
                                   String pdfUrl, String transactionRef) {}
