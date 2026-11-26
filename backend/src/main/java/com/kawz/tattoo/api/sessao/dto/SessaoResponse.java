package com.kawz.tattoo.api.sessao.dto;

import java.time.LocalDateTime;

public record SessaoResponse(Integer id, Integer projetoId, LocalDateTime dataHorario,
                                 String localAtendimento, String statusSessao, String observacoesSessao,
                                 Integer duracaoHoras) {}
