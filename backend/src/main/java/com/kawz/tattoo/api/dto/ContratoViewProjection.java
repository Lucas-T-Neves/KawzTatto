package com.kawz.tattoo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projeção da view vw_contratos. Os aliases em aspas duplas preservam o
 * camelCase exigido pelo mapeamento coluna -> propriedade (DATABASE_TO_LOWER=TRUE).
 */
public interface ContratoViewProjection {
    Integer getContratoId();
    String getNomeCliente();
    String getTelefoneCliente();
    String getCpfCliente();
    String getNomeArtista();
    String getDescricaoTatuagem();
    String getLocalCorpo();
    LocalDate getDataInicio();
    LocalDate getDataFim();
    BigDecimal getValorOrcado();
    /** H2 devolve boolean; MySQL devolve 0/1 (número). Use ApiMapper.paraBoolean. */
    Object getSinalPago();
    String getFormaPagamento();
    String getLimiteAlteracoesArte();
    String getToleranciaAtraso();
    String getUsoImagem();
    LocalDate getDataAssinatura();
    String getStatusContrato();
    String getPdfUrl();
}
