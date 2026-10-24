package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.PreferenciasCliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenciasClienteRepository extends JpaRepository<PreferenciasCliente, Integer> {
}
