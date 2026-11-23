package com.kawz.tattoo.domain.orcamento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Integer> {

    @Query("""
            SELECT o FROM Orcamento o
            WHERE o.projeto.tatuador.id = :tatuadorId
              AND (:status IS NULL OR LOWER(o.status) = LOWER(:status))
              AND (:projetoId IS NULL OR o.projeto.id = :projetoId)
              AND (:clienteId IS NULL OR o.cliente.id = :clienteId)
            ORDER BY o.dataOrcamento DESC, o.id DESC
            """)
    Page<Orcamento> buscar(@Param("tatuadorId") Integer tatuadorId,
                           @Param("status") String status,
                           @Param("projetoId") Integer projetoId,
                           @Param("clienteId") Integer clienteId,
                           Pageable pageable);

    List<Orcamento> findByProjetoIdAndProjeto_Tatuador_IdOrderByDataOrcamentoDescIdDesc(
            Integer projetoId, Integer tatuadorId);

    Optional<Orcamento> findByIdAndProjeto_Tatuador_Id(Integer id, Integer tatuadorId);
}
