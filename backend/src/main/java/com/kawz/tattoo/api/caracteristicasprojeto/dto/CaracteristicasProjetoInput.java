package com.kawz.tattoo.api.caracteristicasprojeto.dto;

import jakarta.validation.constraints.Size;

public record CaracteristicasProjetoInput(@Size(max = 10000) String ideiaExpectativa,
                                              @Size(max = 45) String estiloArte,
                                              @Size(max = 45) String localizacaoCorpo,
                                              @Size(max = 45) String tamanho,
                                              @Size(max = 45) String dificuldade,
                                              @Size(max = 45) String cores,
                                              @Size(max = 45) String complexidade) {}
