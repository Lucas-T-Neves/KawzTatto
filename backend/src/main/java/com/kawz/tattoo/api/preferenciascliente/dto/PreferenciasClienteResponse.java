package com.kawz.tattoo.api.preferenciascliente.dto;

import java.math.BigDecimal;

public record PreferenciasClienteResponse(Integer id, Integer clienteId, String musicaFavorita,
                                              String comidaFavorita, String estiloTatuagemFavorita,
                                              BigDecimal faixaMediaOrcamento, String faixaOrcamento, String habitos,
                                              String disponibilidadeHorarios, String observacoesGerais) {}
