package com.kawz.tattoo.api.imagem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ImagemInput(@NotNull Integer projetoId,
                              @NotBlank @Size(max = 255) String publicId,
                              @NotBlank @Size(max = 1000) String url,
                              @Size(max = 20) String formato,
                              @Size(max = 100) String bytes,
                              Integer larguraAltura) {}
