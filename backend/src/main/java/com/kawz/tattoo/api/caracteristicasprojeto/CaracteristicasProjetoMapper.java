package com.kawz.tattoo.api.caracteristicasprojeto;

import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoInput;
import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.domain.caracteristicasprojeto.CaracteristicasProjeto;


public final class CaracteristicasProjetoMapper {
    private CaracteristicasProjetoMapper() {}

    public static CaracteristicasProjetoResponse toResponse(CaracteristicasProjeto value, Integer projetoId) {
        return new CaracteristicasProjetoResponse(
                value.getId(),
                projetoId,
                value.getIdeiaExpectativa(),
                value.getEstiloArte(),
                value.getLocalizacaoCorpo(),
                value.getTamanho(),
                value.getDificuldade(),
                value.getCores(),
                value.getComplexidade());
    }

    public static CaracteristicasProjeto toEntity(CaracteristicasProjetoInput value) {
        return new CaracteristicasProjeto(
                value.ideiaExpectativa(),
                value.estiloArte(),
                value.localizacaoCorpo(),
                value.tamanho(),
                value.dificuldade(),
                value.cores(),
                value.complexidade());
    }
}
