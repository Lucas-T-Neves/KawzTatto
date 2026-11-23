package com.kawz.tattoo.api.atividade;

import com.kawz.tattoo.api.atividade.dto.AtividadeInput;
import com.kawz.tattoo.api.atividade.dto.AtividadeResponse;
import com.kawz.tattoo.domain.atividade.Atividade;
import com.kawz.tattoo.domain.tatuador.Tatuador;


public final class AtividadeMapper {
    private AtividadeMapper() {}

    public static AtividadeResponse toResponse(Atividade atividade) {
        AtividadeResponse r = new AtividadeResponse(atividade.getId(),
                                      atividade.getTatuador().getId(),
                                             atividade.getProjetoId(),
                                             atividade.getClienteId(),
                                             atividade.getDescricao(),
                                                  atividade.getTipo(),
                                                atividade.getStatus(),
                                                atividade.getTitulo(),
                                                  atividade.getData(),
                                                atividade.getInicio(),
                                               atividade.getTermino(),
                                                 atividade.getLocal(),
                                          atividade.getObservacoes());
    return r;
    }
    public static Atividade toEntity(AtividadeInput atividade, Tatuador tatuador) {
        Atividade a = new Atividade( tatuador,
                        atividade.projetoId(),
                        atividade.clienteId(),
                        atividade.descricao(),
                             atividade.tipo(),
                           atividade.status(),
                           atividade.titulo(),
                             atividade.data(),
                           atividade.inicio(),
                          atividade.termino(),
                            atividade.local(),
                     atividade.observacoes());
        return a;
    }
}
