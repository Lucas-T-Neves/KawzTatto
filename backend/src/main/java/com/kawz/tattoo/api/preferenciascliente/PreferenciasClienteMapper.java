package com.kawz.tattoo.api.preferenciascliente;

import com.kawz.tattoo.api.preferenciascliente.dto.PreferenciasClienteResponse;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasCliente;


public final class PreferenciasClienteMapper {
    private PreferenciasClienteMapper() {}

    public static PreferenciasClienteResponse toResponse(PreferenciasCliente value, Integer clienteId) {
        return new PreferenciasClienteResponse(value.getId(), clienteId, value.getMusicaFavorita(),
                value.getComidaFavorita(), value.getEstiloTatuagemFavorita(), value.getFaixaMediaOrcamento(),
                value.getFaixaOrcamento(),
                value.getHabitos(), value.getDisponibilidadeHorarios(), value.getObservacoesGerais());
    }
}
