package com.kawz.tattoo.domain.tatuador;

import com.kawz.tattoo.domain.tatuador.Tatuador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TatuadorRepository extends JpaRepository<Tatuador, Integer> {
    Optional<Tatuador> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
}
