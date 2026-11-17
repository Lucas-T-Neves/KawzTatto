package com.kawz.tattoo.api.movimentacao.mapper;

import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoFinanceira;


public final class MovimentacaoApiMapper {
    private MovimentacaoApiMapper() {}

    public static MovimentacaoResponse toResponse(MovimentacaoFinanceira value) {
        return new MovimentacaoResponse(value.getId(), value.getProjeto().getId(), value.getNaturezaTransacao(),
                value.getCategoria(), value.getDataOcorrencia(), value.getValorMonetario(),
                value.getFormaPagamento(), value.getStatusPagamento());
    }
}
