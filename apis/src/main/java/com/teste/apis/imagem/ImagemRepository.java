package com.teste.apis.imagem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagemRepository extends JpaRepository<Imagem, Long> {

    Optional<Imagem> findByPublicId(String publicId);

    void deleteByPublicId(String publicId);

    /** Listagem do controller: do mais recente para o mais antigo. */
    List<Imagem> findAllByOrderByCriadoEmDesc();
}
