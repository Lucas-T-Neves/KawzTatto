package com.kawz.tattoo.domain.cliente;

import com.kawz.tattoo.domain.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
