package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnamneseRepository extends JpaRepository<Anamnese, Integer> {
    List<Anamnese> findByClienteIdOrderByIdDesc(Integer clienteId);
}
