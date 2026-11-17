package com.kawz.tattoo.api.tatuador.mapper;

import com.kawz.tattoo.api.tatuador.dto.TatuadorResponse;
import com.kawz.tattoo.domain.tatuador.Tatuador;


public final class TatuadorApiMapper {
    private TatuadorApiMapper() {}

    public static TatuadorResponse toResponse(Tatuador value) {
        return new TatuadorResponse(value.getId(), value.getNome(), value.getEmail());
    }
}
