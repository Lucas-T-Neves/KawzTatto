package com.kawz.tattoo.api.tatuador.dto;

import java.time.LocalDateTime;

public record LoginResponse(boolean autenticado, Integer tatuadorId, String nome,
                                String token, LocalDateTime expiraEm) {}
