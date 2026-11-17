package com.kawz.tattoo.api.preferenciascliente.mapper;

import com.kawz.tattoo.api.preferenciascliente.dto.PreferenciasClienteResponse;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasCliente;


public final class PreferenciasclienteApiMapper {
    private PreferenciasclienteApiMapper() {}

    public static PreferenciasClienteResponse toResponse(PreferenciasCliente value, Integer clienteId) {
        return new PreferenciasClienteResponse(value.getId(), clienteId, value.getMusicaFavorita(),
                value.getComidaFavorita(), value.getEstiloTatuagemFavorita(), value.getFaixaMediaOrcamento(),
                value.getFaixaOrcamento(),
                value.getHabitos(), value.getDisponibilidadeHorarios(), value.getObservacoesGerais());
    }
}
