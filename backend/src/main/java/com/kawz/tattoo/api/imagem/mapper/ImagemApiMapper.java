package com.kawz.tattoo.api.imagem.mapper;

import com.kawz.tattoo.api.imagem.dto.ImagemResponse;
import com.kawz.tattoo.domain.imagem.Imagem;


public final class ImagemApiMapper {
    private ImagemApiMapper() {}

    public static ImagemResponse toResponse(Imagem value) {
        return new ImagemResponse(value.getId(), value.getProjeto().getId(), value.getPublicId(), value.getUrl(),
                nomeImagem(value),
                value.getFormato(), value.getBytes(), value.getLarguraAltura(), value.getCriadoEm());
    }

    private static String nomeImagem(Imagem imagem) {
        String publicId = imagem.getPublicId();
        if (publicId == null || publicId.isBlank()) return null;
        int separador = publicId.lastIndexOf('/');
        String nome = separador >= 0 ? publicId.substring(separador + 1) : publicId;
        return nome.isBlank() ? publicId : nome;
    }
}
