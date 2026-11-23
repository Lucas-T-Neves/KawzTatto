package com.kawz.tattoo.api.orcamento;

import com.kawz.tattoo.api.orcamento.dto.OrcamentoResponse;
import com.kawz.tattoo.domain.orcamento.Orcamento;


public final class OrcamentoMapper {
    private OrcamentoMapper() {}

    public static OrcamentoResponse toResponse(Orcamento value) {
        return new OrcamentoResponse(value.getId(), value.getProjeto().getId(), value.getCliente().getId(),
                value.getNome(), value.getDataOrcamento(), value.getStatus(), value.getValorTotal(),
                value.getValorSinal(), value.getQuantidadeSessoes(), value.valorSessaoEstimado(),
                value.getFormaPagamento(), value.getDataValidade(), value.getPropostaAceita());
    }
}
