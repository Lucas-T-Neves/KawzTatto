package com.kawz.tattoo.api.atividade.mapper;

import com.kawz.tattoo.api.atividade.dto.AtividadeResponse;
import com.kawz.tattoo.domain.atividade.Atividade;


public final class AtividadeApiMapper {
    private AtividadeApiMapper() {}

    public static AtividadeResponse toResponse(Atividade value) {
        return new AtividadeResponse(value.getId(), value.getTatuador().getId(), value.getProjetoId(),
                value.getClienteId(), value.getDescricao(), value.getTipo(), value.getStatus(),
                value.getTitulo(), value.getData(), value.getInicio(), value.getTermino(),
                value.getLocal(), value.getObservacoes());
    }
}
