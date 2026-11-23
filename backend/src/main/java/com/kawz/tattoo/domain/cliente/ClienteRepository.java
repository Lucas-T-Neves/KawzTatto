package com.kawz.tattoo.domain.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Consultas de cliente sempre filtram pelo tatuador autenticado. Não existe
 * busca por id sem proprietário para uso na API: quem não é dono recebe 404.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Page<Cliente> findByTatuadorId(Integer tatuadorId, Pageable pageable);

    Optional<Cliente> findByIdAndTatuadorId(Integer id, Integer tatuadorId);

    boolean existsByIdAndTatuadorId(Integer id, Integer tatuadorId);
}
