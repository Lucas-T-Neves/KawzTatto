package com.kawz.tattoo.domain.sessao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgendaSyncPendenteRepository extends JpaRepository<AgendaSyncPendente, Integer> {
    List<AgendaSyncPendente> findTop100ByOrderByIdAsc();
    boolean existsBySessaoId(Integer sessaoId);
    void deleteBySessaoId(Integer sessaoId);
}
