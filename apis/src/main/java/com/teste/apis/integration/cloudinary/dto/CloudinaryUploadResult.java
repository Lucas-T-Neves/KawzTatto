package com.teste.apis.integration.cloudinary.dto;

/** Resultado do upload, no formato do nosso dominio (nada do mapa do SDK). */
public record CloudinaryUploadResult(
        String publicId,
        String url,
        String formato,
        Long bytes,
        Integer largura,
        Integer altura
) {
}
