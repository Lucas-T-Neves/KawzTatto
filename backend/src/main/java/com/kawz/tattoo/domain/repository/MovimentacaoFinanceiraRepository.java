package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.MovimentacaoFinanceira;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Integer> {
    Page<MovimentacaoFinanceira> findByProjetoIdOrderByDataOcorrenciaDesc(Integer projetoId, Pageable pageable);

    @Query("""
            SELECT m FROM MovimentacaoFinanceira m
            WHERE (:inicio IS NULL OR m.dataOcorrencia >= :inicio)
              AND (:fim IS NULL OR m.dataOcorrencia < :fim)
              AND (:status IS NULL OR LOWER(m.statusPagamento) = LOWER(:status))
              AND (:projetoId IS NULL OR m.projeto.id = :projetoId)
              AND (:clienteId IS NULL OR m.projeto.cliente.id = :clienteId)
            ORDER BY m.dataOcorrencia DESC, m.id DESC
            """)
    Page<MovimentacaoFinanceira> buscar(@Param("inicio") LocalDate inicio,
                                        @Param("fim") LocalDate fim,
                                        @Param("status") String status,
                                        @Param("projetoId") Integer projetoId,
                                        @Param("clienteId") Integer clienteId,
                                        Pageable pageable);

    List<MovimentacaoFinanceira> findByDataOcorrenciaGreaterThanEqualAndDataOcorrenciaLessThan(
            LocalDate inicio, LocalDate fim);

    /** Soma das entradas pagas: base do saldo a receber (total - recebido). */
    @Query("""
            SELECT COALESCE(SUM(m.valorMonetario), 0)
            FROM MovimentacaoFinanceira m
            WHERE m.projeto.id = :projetoId
              AND UPPER(m.naturezaTransacao) = 'ENTRADA'
              AND LOWER(m.statusPagamento) = 'pago'
            """)
    BigDecimal somarRecebido(@Param("projetoId") Integer projetoId);

    /** Existe movimentação de sinal paga? Mesma regra da view vw_contratos (sinal_pago). */
    boolean existsByProjetoIdAndCategoriaIgnoreCaseAndStatusPagamentoIgnoreCase(
            Integer projetoId, String categoria, String statusPagamento);

    /** Resumo por projeto: [projetoId, recebido, sinalPago(0/1)] para as listagens. */
    @Query("""
            SELECT m.projeto.id,
                   COALESCE(SUM(CASE WHEN UPPER(m.naturezaTransacao) = 'ENTRADA' AND LOWER(m.statusPagamento) = 'pago'
                                     THEN m.valorMonetario ELSE 0 END), 0),
                   MAX(CASE WHEN LOWER(m.categoria) = 'sinal' AND LOWER(m.statusPagamento) = 'pago'
                            THEN 1 ELSE 0 END)
            FROM MovimentacaoFinanceira m
            GROUP BY m.projeto.id
            """)
    List<Object[]> resumirPorProjeto();
}
