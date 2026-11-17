package com.kawz.tattoo.api.sessao.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SessaoRemarcarInput(@NotNull LocalDateTime dataHorario) {}
