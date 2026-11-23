package com.kawz.tattoo.domain.anamnese;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnamneseRepository extends JpaRepository<Anamnese, Integer> {

    List<Anamnese> findByClienteIdAndCliente_Tatuador_IdOrderByIdDesc(Integer clienteId, Integer tatuadorId);

    Optional<Anamnese> findByIdAndCliente_Tatuador_Id(Integer id, Integer tatuadorId);
}
