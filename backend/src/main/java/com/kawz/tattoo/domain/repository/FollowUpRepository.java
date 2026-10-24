package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUp, Integer> {
    List<FollowUp> findByClienteIdOrderByDataPrevistaAsc(Integer clienteId);

    @Query("""
            SELECT DISTINCT f FROM FollowUp f
            JOIN Projeto p ON p.cliente.id = f.cliente.id
            WHERE p.tatuador.id = :tatuadorId
            ORDER BY f.dataPrevista ASC, f.id ASC
            """)
    List<FollowUp> findByTatuadorId(@Param("tatuadorId") Integer tatuadorId);
}
