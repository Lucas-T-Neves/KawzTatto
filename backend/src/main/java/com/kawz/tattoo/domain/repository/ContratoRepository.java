package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.api.dto.ContratoViewProjection;
import com.kawz.tattoo.domain.model.Contrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    @Query(value = """
            SELECT v.contrato_id       AS "contratoId",
                   v.nome_cliente      AS "nomeCliente",
                   v.telefone_cliente  AS "telefoneCliente",
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
                   v.data_assinatura   AS "dataAssinatura"
            FROM vw_contratos v
            ORDER BY v.contrato_id
            """,
            countQuery = "SELECT COUNT(*) FROM vw_contratos v",
            nativeQuery = true)
    Page<ContratoViewProjection> buscarNaView(Pageable pageable);

    @Query(value = """
            SELECT v.contrato_id       AS "contratoId",
                   v.nome_cliente      AS "nomeCliente",
                   v.telefone_cliente  AS "telefoneCliente",
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
                   v.data_assinatura   AS "dataAssinatura"
            FROM vw_contratos v
            WHERE v.contrato_id = :id
            """, nativeQuery = true)
    Optional<ContratoViewProjection> buscarNaViewPorId(@Param("id") Integer id);
}
