package com.kawz.tattoo.domain.repository;

import com.kawz.tattoo.domain.model.Imagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImagemRepository extends JpaRepository<Imagem, Integer> {
    List<Imagem> findByProjetoIdOrderByCriadoEmDesc(Integer projetoId);
    Optional<Imagem> findFirstByProjetoIdOrderByCriadoEmDesc(Integer projetoId);
    /** Só os publicIds (consulta escalar: não carrega entidades, útil antes de excluir o projeto). */
    @Query("select i.publicId from Imagem i where i.projeto.id = :projetoId")
    List<String> listarPublicIds(@Param("projetoId") Integer projetoId);
    boolean existsByPublicId(String publicId);
    boolean existsByPublicIdAndIdNot(String publicId, Integer id);
}
