package com.teste.apis.contrato;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    /** Listagem do controller: do mais recente para o mais antigo. */
    List<Contrato> findAllByOrderByCriadoEmDesc();
}
