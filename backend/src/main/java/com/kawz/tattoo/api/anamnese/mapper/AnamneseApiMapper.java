package com.kawz.tattoo.api.anamnese.mapper;

import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.domain.anamnese.Anamnese;


public final class AnamneseApiMapper {
    private AnamneseApiMapper() {}

    public static AnamneseResponse toResponse(Anamnese value) {
        return new AnamneseResponse(value.getId(), value.getCliente().getId(), value.getDadosSaude(),
                value.getPreparacaoTatuagem());
    }
}
