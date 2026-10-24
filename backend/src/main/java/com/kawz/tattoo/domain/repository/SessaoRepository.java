package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Sessao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, Integer> {
    Page<Sessao> findByProjetoIdOrderByDataHorarioAsc(Integer projetoId, Pageable pageable);
    Page<Sessao> findByDataHorarioAfterOrderByDataHorarioAsc(java.time.LocalDateTime dataHorario, Pageable pageable);
}
