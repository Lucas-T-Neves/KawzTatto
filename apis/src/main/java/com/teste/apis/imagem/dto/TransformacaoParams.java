package com.teste.apis.imagem.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Parametros de GET /imagens/{id}/url. Todos opcionais e restritos a
 * listas fechadas, para nao montar transformacao arbitraria a partir da
 * entrada do usuario. Valores fora da lista sao rejeitados com 400.
 */
public record TransformacaoParams(

        @Pattern(regexp = "^(auto|[1-9][0-9]{0,3}|10000)$",
                message = "largura deve ser 'auto' ou um numero de 1 a 10000")
        String largura,

        @Pattern(regexp = "^(auto|[1-9][0-9]{0,3}|10000)$",
                message = "altura deve ser 'auto' ou um numero de 1 a 10000")
        String altura,

        @Pattern(regexp = "^(?i)(fill|fit|crop|scale)$",
                message = "corte deve ser fill, fit, crop ou scale")
        String corte,

        @Pattern(regexp = "^(?i)(auto|[1-9][0-9]?|100)$",
                message = "qualidade deve ser 'auto' ou um numero de 1 a 100")
        String qualidade,

        @Pattern(regexp = "^(?i)(auto|webp|jpg|png)$",
                message = "formato deve ser auto, webp, jpg ou png")
        String formato,

        @Pattern(regexp = "^(?i)(grayscale|sepia|blur)$",
                message = "efeito deve ser grayscale, sepia ou blur")
        String efeito
) {
}
