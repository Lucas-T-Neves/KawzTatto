package com.kawz.tattoo.api.preferenciascliente.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PreferenciasClienteInput(@Size(max = 120) String musicaFavorita,
                                           @Size(max = 120) String comidaFavorita,
                                           @Size(max = 120) String estiloTatuagemFavorita,
                                           @DecimalMin("0.00") BigDecimal faixaMediaOrcamento,
                                           @Size(max = 120) String faixaOrcamento,
                                           @Size(max = 255) String habitos,
                                           @Size(max = 255) String disponibilidadeHorarios,
                                           @Size(max = 10000) String observacoesGerais) {}
