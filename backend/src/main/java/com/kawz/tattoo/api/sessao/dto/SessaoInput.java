package com.kawz.tattoo.api.sessao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record SessaoInput(
            @NotNull Integer projetoId,
            @NotNull LocalDateTime dataHorario,
            @NotBlank @Size(max = 45) String localAtendimento,
            @NotBlank @Size(max = 45) String statusSessao,
            @Size(max = 45) String observacoesSessao) {}
