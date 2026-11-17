package com.kawz.tattoo.api.contrato.dto;

import jakarta.validation.constraints.Size;

/** Dados opcionais que não existem no banco e entram no PDF do contrato. */
public record ContratoPdfInput(@Size(max = 20) String cpfCliente,
                                   @Size(max = 100) String tamanhoEstimado) {}
