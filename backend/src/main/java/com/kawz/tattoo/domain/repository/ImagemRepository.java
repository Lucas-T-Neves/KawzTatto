package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Imagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagemRepository extends JpaRepository<Imagem, Integer> {
    List<Imagem> findByProjetoIdOrderByCriadoEmDesc(Integer projetoId);
    boolean existsByPublicId(String publicId);
    boolean existsByPublicIdAndIdNot(String publicId, Integer id);
}
