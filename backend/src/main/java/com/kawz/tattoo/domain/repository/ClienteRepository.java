package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
