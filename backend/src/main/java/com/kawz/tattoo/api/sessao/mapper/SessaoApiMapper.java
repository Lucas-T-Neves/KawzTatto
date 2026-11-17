package com.kawz.tattoo.api.sessao.mapper;

import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.domain.sessao.Sessao;


public final class SessaoApiMapper {
    private SessaoApiMapper() {}

    public static SessaoResponse toResponse(Sessao value) {
        return new SessaoResponse(value.getId(), value.getProjeto().getId(), value.getDataHorario(),
                value.getLocalAtendimento(), value.getStatusSessao(), value.getObservacoesSessao());
    }
}
