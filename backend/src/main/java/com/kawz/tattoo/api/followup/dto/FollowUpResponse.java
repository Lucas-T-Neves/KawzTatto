package com.kawz.tattoo.api.followup.dto;

import java.time.LocalDate;

public record FollowUpResponse(Integer id, Integer clienteId, String tipoContato, String statusContato,
                                   LocalDate dataPrevista, LocalDate dataRealizacao,
                                   String descricaoObservacao, String proximaAcao) {}
