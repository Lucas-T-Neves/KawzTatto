package com.kawz.tattoo.api.projeto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoInput(
            @NotNull Integer tatuadorId,
            @NotNull Integer clienteId,
            @Size(max = 150) String nome,
            @NotBlank @Size(max = 10000) String descricao,
            @Size(max = 255) String linkPasta,
            @NotBlank @Size(max = 45) String statusProjeto,
            @NotNull @DecimalMin("0.00") BigDecimal valorTotal,
            @NotNull @DecimalMin("0.00") BigDecimal valorSinal,
            @NotNull @Min(1) Integer quantidadeSessoes,
            LocalDate dataValidade,
            @Size(max = 45) String lembretePre,
            @Size(max = 45) String lembretePost,
            @Size(max = 10000) String ideiaExpectativa,
            @Size(max = 45) String estiloArte,
            @Size(max = 45) String localizacaoCorpo,
            @Size(max = 45) String tamanho,
            @Size(max = 45) String dificuldade,
            @Size(max = 45) String cores,
            @Size(max = 45) String complexidade) {}
