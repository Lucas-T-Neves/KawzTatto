package com.teste.apis.integration.cloudinary.dto;

/**
 * Transformacao pedida, ja validada. Os valores chegam normalizados
 * (minusculos) e sao sempre de uma lista fechada definida na API.
 */
public record TransformacaoUrl(
        String largura,
        String altura,
        String corte,
        String qualidade,
        String formato,
        String efeito
) {
}
