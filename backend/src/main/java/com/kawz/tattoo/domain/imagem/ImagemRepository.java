package com.kawz.tattoo.domain.imagem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImagemRepository extends JpaRepository<Imagem, Integer> {

    List<Imagem> findByProjetoIdAndProjeto_Tatuador_IdOrderByCriadoEmDesc(Integer projetoId, Integer tatuadorId);

    Optional<Imagem> findFirstByProjetoIdAndProjeto_Tatuador_IdOrderByCriadoEmDesc(Integer projetoId, Integer tatuadorId);

    Optional<Imagem> findByIdAndProjeto_Tatuador_Id(Integer id, Integer tatuadorId);

    /** Só os publicIds (consulta escalar: não carrega entidades, útil antes de excluir o projeto). */
    @Query("select i.publicId from Imagem i where i.projeto.id = :projetoId and i.projeto.tatuador.id = :tatuadorId")
    List<String> listarPublicIds(@Param("projetoId") Integer projetoId, @Param("tatuadorId") Integer tatuadorId);

    boolean existsByPublicId(String publicId);

    boolean existsByPublicIdAndIdNot(String publicId, Integer id);
}
