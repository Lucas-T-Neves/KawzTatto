package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Integer> {
    List<Atividade> findByTatuadorIdOrderByIdDesc(Integer tatuadorId);
}
