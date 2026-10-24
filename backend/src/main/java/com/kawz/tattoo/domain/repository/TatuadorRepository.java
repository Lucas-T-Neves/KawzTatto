package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Tatuador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TatuadorRepository extends JpaRepository<Tatuador, Integer> {
    Optional<Tatuador> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
}
