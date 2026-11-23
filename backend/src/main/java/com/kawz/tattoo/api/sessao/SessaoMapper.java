package com.kawz.tattoo.api.sessao;

import com.kawz.tattoo.api.sessao.dto.SessaoInput;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.sessao.Sessao;


public final class SessaoMapper {
    private SessaoMapper() {}

    public static SessaoResponse toResponse(Sessao value) {
        return new SessaoResponse(value.getId(), value.getProjeto().getId(), value.getDataHorario(),
                value.getLocalAtendimento(), value.getStatusSessao(), value.getObservacoesSessao());
    }

    public static Sessao toEntity(SessaoInput value, Projeto projeto) {
        return new Sessao( projeto, value.dataHorario(),
                value.localAtendimento(), value.statusSessao(), value.observacoesSessao());
    }
}
