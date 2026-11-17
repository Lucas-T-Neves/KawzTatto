package com.kawz.tattoo.api.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteInput(
            @NotBlank @Size(max = 150) String nome,
            @Size(max = 20) String telefone,
            @Size(max = 20) String cpf,
            @Size(max = 100) String instagram,
            @Size(max = 45) String canalAquisicao,
            @Size(max = 255) String linkConversa,
            @Size(max = 255) String linkPasta,
            @Size(max = 120) String localizacao,
            @Size(max = 45) String tamanho,
            @Size(max = 10000) String ideia,
            @Size(max = 120) String musicaFavorita,
            @Size(max = 120) String comidaFavorita,
            @Size(max = 120) String estiloTatuagemFavorita,
            @Size(max = 120) String faixaOrcamento,
            @Size(max = 255) String habitos,
            @Size(max = 255) String disponibilidadeHorarios,
            @Size(max = 10000) String observacoesGerais,
            @Size(max = 45) String status) {}
