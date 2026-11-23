package com.kawz.tattoo.domain.sessao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessaoRepository extends JpaRepository<Sessao, Integer> {

    Optional<Sessao> findByIdAndProjeto_Tatuador_Id(Integer id, Integer tatuadorId);

    Page<Sessao> findByProjeto_Tatuador_Id(Integer tatuadorId, Pageable pageable);

    Page<Sessao> findByProjetoIdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
            Integer projetoId, Integer tatuadorId, Pageable pageable);

    Page<Sessao> findByProjeto_Cliente_IdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
            Integer clienteId, Integer tatuadorId, Pageable pageable);

    Page<Sessao> findByDataHorarioAfterAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
            LocalDateTime dataHorario, Integer tatuadorId, Pageable pageable);

    @Query("""
            SELECT s FROM Sessao s
            WHERE s.dataHorario >= :inicio AND s.dataHorario < :fim
              AND s.projeto.tatuador.id = :tatuadorId
            ORDER BY s.dataHorario ASC
            """)
    Page<Sessao> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim,
                                  @Param("tatuadorId") Integer tatuadorId,
                                  Pageable pageable);

    /** Todas as sessões de um projeto do tatuador (usado no projeto detalhado). */
    List<Sessao> findByProjetoIdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(Integer projetoId, Integer tatuadorId);
}
