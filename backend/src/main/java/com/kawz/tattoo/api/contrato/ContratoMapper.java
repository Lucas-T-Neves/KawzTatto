package com.kawz.tattoo.api.contrato;

import com.kawz.tattoo.api.contrato.dto.ContratoResponse;
import com.kawz.tattoo.api.contrato.dto.ContratoViewResponse;
import com.kawz.tattoo.domain.contrato.Contrato;
import com.kawz.tattoo.domain.contrato.ContratoViewProjection;


public final class ContratoMapper {
    private ContratoMapper() {}

    public static ContratoResponse toResponse(Contrato value) {
        return new ContratoResponse(value.getId(), value.getLimiteAlteracoesArte(), value.getToleranciaAtraso(),
                value.getUsoImagem(), value.getDataAssinatura(),
                value.getStatusContrato(), value.getCriadoEm(), value.getPdfUrl(), value.getTransactionRef());
    }

    public static Boolean paraBoolean(Object valor) {
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Number n) return n.intValue() != 0;
        return Boolean.FALSE;
    }

    public static ContratoViewResponse toResponse(ContratoViewProjection value) {
        return new ContratoViewResponse(value.getContratoId(), value.getNomeCliente(), value.getTelefoneCliente(),
                value.getCpfCliente(), value.getNomeArtista(), value.getDescricaoTatuagem(), value.getLocalCorpo(),
                value.getDataInicio(), value.getDataFim(), value.getValorOrcado(), paraBoolean(value.getSinalPago()),
                value.getFormaPagamento(), value.getLimiteAlteracoesArte(), value.getToleranciaAtraso(),
                value.getUsoImagem(), value.getDataAssinatura(), value.getStatusContrato(), value.getPdfUrl());
    }
}
