package com.kawz.tattoo.api.anamnese.dto;


public record AnamneseResponse(Integer id, Integer clienteId, String dadosSaude,
                                   String preparacaoTatuagem) {}
