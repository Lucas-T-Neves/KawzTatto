package com.kawz.tattoo.domain.followup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FollowUpRepository extends JpaRepository<FollowUp, Integer> {

    List<FollowUp> findByClienteIdAndCliente_Tatuador_IdOrderByDataPrevistaAsc(Integer clienteId, Integer tatuadorId);

    List<FollowUp> findByCliente_Tatuador_Id(Integer tatuadorId);

    List<FollowUp> findByStatusContatoIgnoreCaseAndCliente_Tatuador_IdOrderByDataPrevistaAsc(
            String statusContato, Integer tatuadorId);

    List<FollowUp> findByDataPrevistaLessThanEqualAndCliente_Tatuador_IdOrderByDataPrevistaAsc(
            LocalDate data, Integer tatuadorId);

    Optional<FollowUp> findByIdAndCliente_Tatuador_Id(Integer id, Integer tatuadorId);

    @Query("""
            SELECT DISTINCT f FROM FollowUp f
            JOIN Projeto p ON p.cliente.id = f.cliente.id
            WHERE p.id = :projetoId AND p.tatuador.id = :tatuadorId
            ORDER BY f.dataPrevista ASC, f.id ASC
            """)
    List<FollowUp> findByProjetoIdDoTatuador(@Param("projetoId") Integer projetoId,
                                             @Param("tatuadorId") Integer tatuadorId);
}
