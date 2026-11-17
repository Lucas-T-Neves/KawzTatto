package com.kawz.tattoo.api.caracteristicasprojeto.dto;


public record CaracteristicasProjetoResponse(Integer id, Integer projetoId, String ideiaExpectativa,
                                                 String estiloArte, String localizacaoCorpo, String tamanho,
                                                 String dificuldade, String cores, String complexidade) {}
