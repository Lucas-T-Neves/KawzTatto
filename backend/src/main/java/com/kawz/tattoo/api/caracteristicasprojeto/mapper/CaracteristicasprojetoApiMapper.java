package com.kawz.tattoo.api.caracteristicasprojeto.mapper;

import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.domain.caracteristicasprojeto.CaracteristicasProjeto;


public final class CaracteristicasprojetoApiMapper {
    private CaracteristicasprojetoApiMapper() {}

    public static CaracteristicasProjetoResponse toResponse(CaracteristicasProjeto value, Integer projetoId) {
        return new CaracteristicasProjetoResponse(value.getId(), projetoId, value.getIdeiaExpectativa(),
                value.getEstiloArte(), value.getLocalizacaoCorpo(), value.getTamanho(), value.getDificuldade(),
                value.getCores(), value.getComplexidade());
    }
}
