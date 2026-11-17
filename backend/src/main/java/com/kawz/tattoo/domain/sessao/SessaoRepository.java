package com.kawz.tattoo.domain.sessao;

import com.kawz.tattoo.domain.sessao.Sessao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessaoRepository extends JpaRepository<Sessao, Integer> {
    Page<Sessao> findByProjetoIdOrderByDataHorarioAsc(Integer projetoId, Pageable pageable);
    Page<Sessao> findByProjetoClienteIdOrderByDataHorarioAsc(Integer clienteId, Pageable pageable);
    Page<Sessao> findByDataHorarioAfterOrderByDataHorarioAsc(java.time.LocalDateTime dataHorario, Pageable pageable);
    @Query("""
            SELECT s FROM Sessao s
            WHERE s.dataHorario >= :inicio AND s.dataHorario < :fim
            ORDER BY s.dataHorario ASC
            """)
    Page<Sessao> buscarPorPeriodo(@Param("inicio") java.time.LocalDateTime inicio,
                                  @Param("fim") java.time.LocalDateTime fim,
                                  Pageable pageable);
}
