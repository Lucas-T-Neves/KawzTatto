package com.kawz.tattoo.domain.projeto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjetoRepository extends JpaRepository<Projeto, Integer> {

    Page<Projeto> findByTatuadorId(Integer tatuadorId, Pageable pageable);

    Optional<Projeto> findByIdAndTatuadorId(Integer id, Integer tatuadorId);

    List<Projeto> findByClienteIdAndTatuadorIdOrderByIdDesc(Integer clienteId, Integer tatuadorId);

    List<Projeto> findByTatuadorIdOrderByIdDesc(Integer tatuadorId);

    /** Contrato é mapeado pelo projeto (projeto.contrato_id): a posse vem do projeto. */
    Optional<Projeto> findByContratoIdAndTatuadorId(Integer contratoId, Integer tatuadorId);

    /**
     * Projeto com contrato e características carregados na mesma consulta.
     * Usado pelo caso de uso de projeto detalhado, que devolve o resultado para
     * a camada de API montar a resposta fora da transação.
     */
    @Query("""
            SELECT p FROM Projeto p
            JOIN FETCH p.contrato
            JOIN FETCH p.caracteristicas
            WHERE p.id = :id AND p.tatuador.id = :tatuadorId
            """)
    Optional<Projeto> buscarComDetalhesDoTatuador(@Param("id") Integer id, @Param("tatuadorId") Integer tatuadorId);
}
