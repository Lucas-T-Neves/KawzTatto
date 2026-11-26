package com.kawz.tattoo.api.tatuador;

import com.kawz.tattoo.api.tatuador.dto.TatuadorResponse;
import com.kawz.tattoo.domain.tatuador.Tatuador;


public final class TatuadorMapper {
    private TatuadorMapper() {}

    public static TatuadorResponse toResponse(Tatuador value) {
        return new TatuadorResponse(value.getId(), value.getNome(), value.getEmail(), value.getLogoUrl(), value.getLogoPublicId());
    }
}
