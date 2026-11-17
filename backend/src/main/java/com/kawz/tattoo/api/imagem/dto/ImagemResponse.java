package com.kawz.tattoo.api.imagem.dto;

import java.time.LocalDateTime;

public record ImagemResponse(Integer id, Integer projetoId, String publicId, String url,
                                 String nome,
                                 String formato, String bytes, Integer larguraAltura,
                                 LocalDateTime criadoEm) {}
