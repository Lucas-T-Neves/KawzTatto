package com.kawz.tattoo.domain.contrato;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    String COLUNAS_VIEW = """
            SELECT v.contrato_id       AS "contratoId",
                   v.nome_cliente      AS "nomeCliente",
                   v.telefone_cliente  AS "telefoneCliente",
                   v.cpf_cliente       AS "cpfCliente",
                   v.nome_artista      AS "nomeArtista",
                   v.descricao_tatuagem AS "descricaoTatuagem",
                   v.local_corpo       AS "localCorpo",
                   v.data_inicio       AS "dataInicio",
                   v.data_fim          AS "dataFim",
                   v.valor_orcado      AS "valorOrcado",
                   v.sinal_pago        AS "sinalPago",
                   v.forma_pagamento   AS "formaPagamento",
                   v.limite_alteracoes_arte AS "limiteAlteracoesArte",
                   v.tolerancia_atraso AS "toleranciaAtraso",
                   v.uso_imagem        AS "usoImagem",
                   v.data_assinatura   AS "dataAssinatura",
                   v.status_contrato   AS "statusContrato",
                   v.pdf_url           AS "pdfUrl"
            FROM vw_contratos v
            """;

    /** A view expõe tatuador_id (dono do projeto) para filtrar por proprietário. */
    @Query(value = COLUNAS_VIEW + "WHERE v.tatuador_id = :tatuadorId ORDER BY v.contrato_id",
            countQuery = "SELECT COUNT(*) FROM vw_contratos v WHERE v.tatuador_id = :tatuadorId",
            nativeQuery = true)
    Page<ContratoViewProjection> buscarNaView(@Param("tatuadorId") Integer tatuadorId, Pageable pageable);

    @Query(value = COLUNAS_VIEW + "WHERE v.contrato_id = :id AND v.tatuador_id = :tatuadorId",
            nativeQuery = true)
    Optional<ContratoViewProjection> buscarNaViewPorId(@Param("id") Integer id,
                                                       @Param("tatuadorId") Integer tatuadorId);
}
