package com.teste.apis.imagem.dto;

import java.time.OffsetDateTime;

/** Dados da imagem devolvidos pela API. */
public record ImagemResponseDTO(
        Long id,
        String publicId,
        String url,
        String formato,
        Long bytes,
        Integer largura,
        Integer altura,
        OffsetDateTime criadoEm
) {

    public static ImagemResponseDTO de(com.teste.apis.imagem.Imagem imagem) {
        return new ImagemResponseDTO(
                imagem.getId(),
                imagem.getPublicId(),
                imagem.getUrl(),
                imagem.getFormato(),
                imagem.getBytes(),
                imagem.getLargura(),
                imagem.getAltura(),
                imagem.getCriadoEm());
    }
}
