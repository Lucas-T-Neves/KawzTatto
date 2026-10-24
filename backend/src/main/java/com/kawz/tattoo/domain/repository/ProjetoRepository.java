package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Integer> {
    List<Projeto> findByClienteIdOrderByIdDesc(Integer clienteId);
    List<Projeto> findByTatuadorIdOrderByIdDesc(Integer tatuadorId);
}
