package com.kawz.tattoo.domain.projeto;

import com.kawz.tattoo.domain.projeto.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Integer> {
    List<Projeto> findByClienteIdOrderByIdDesc(Integer clienteId);
    List<Projeto> findByTatuadorIdOrderByIdDesc(Integer tatuadorId);
}
