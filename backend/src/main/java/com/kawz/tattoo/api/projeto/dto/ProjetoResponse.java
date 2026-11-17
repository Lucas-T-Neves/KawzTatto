package com.kawz.tattoo.api.projeto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Saldo é o valor total menos o recebido; sinalPago indica sinal pago nas movimentações. */
public record ProjetoResponse(Integer id, Integer tatuadorId, Integer clienteId, String nome, String descricao,
                                  String linkPasta,
                                  String statusProjeto, BigDecimal valorTotal, BigDecimal valorSinal,
                                  BigDecimal saldo, BigDecimal recebido, Boolean sinalPago,
                                  Integer quantidadeSessoes, LocalDate dataValidade,
                                  String lembretePre, String lembretePost,
                                  String ideiaExpectativa, String estiloArte, String localizacaoCorpo,
                                  String tamanho, String dificuldade, String cores, String complexidade,
                                  Integer referenceId, String referenceName, String referenceUrl,
                                  Integer contratoId, Integer caracteristicasProjetoId) {}
