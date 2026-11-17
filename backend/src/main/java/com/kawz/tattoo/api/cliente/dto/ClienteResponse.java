package com.kawz.tattoo.api.cliente.dto;

import java.time.LocalDateTime;

public record ClienteResponse(Integer id, String nome, String telefone, String cpf, String instagram,
                                  String canalAquisicao, String linkConversa, String linkPasta,
                                  String localizacao, String tamanho, String ideia,
                                  String musicaFavorita, String comidaFavorita, String estiloTatuagemFavorita,
                                  String faixaOrcamento, String habitos, String disponibilidadeHorarios,
                                  String observacoesGerais,
                                  String status, LocalDateTime dataCriacao) {}
