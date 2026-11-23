package com.kawz.tattoo.domain.atividade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepository extends JpaRepository<Atividade, Integer> {

    List<Atividade> findByTatuadorIdOrderByIdDesc(Integer tatuadorId);

    List<Atividade> findByTatuadorIdAndDataOrderByInicioAsc(Integer tatuadorId, LocalDate data);

    List<Atividade> findByTatuadorIdAndDataBetweenOrderByDataAscInicioAsc(Integer tatuadorId, LocalDate inicio, LocalDate fim);

    Optional<Atividade> findByIdAndTatuadorId(Integer id, Integer tatuadorId);
}
