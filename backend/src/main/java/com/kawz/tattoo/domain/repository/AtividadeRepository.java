package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;

public interface AtividadeRepository extends JpaRepository<Atividade, Integer> {
    List<Atividade> findByTatuadorIdOrderByIdDesc(Integer tatuadorId);
    List<Atividade> findByTatuadorIdAndDataOrderByInicioAsc(Integer tatuadorId, LocalDate data);
    List<Atividade> findByTatuadorIdAndDataBetweenOrderByDataAscInicioAsc(Integer tatuadorId, LocalDate inicio, LocalDate fim);
}
