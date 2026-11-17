package com.kawz.tattoo.api.atividade.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AtividadeResponse(Integer id, Integer tatuadorId, Integer projetoId, Integer clienteId,
                                    String descricao, String tipo, String status, String titulo,
                                    LocalDate data, LocalTime inicio, LocalTime termino,
                                    String local, String observacoes) {}
