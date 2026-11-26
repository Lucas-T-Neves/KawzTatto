package com.kawz.tattoo.domain.sessao;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaSyncRetryService {
    private final AgendaSyncPendenteRepository pendencias;
    private final SessaoService sessoes;

    public AgendaSyncRetryService(AgendaSyncPendenteRepository pendencias, SessaoService sessoes) {
        this.pendencias = pendencias;
        this.sessoes = sessoes;
    }

    @Scheduled(fixedDelayString = "${kawz.integracoes.nylas.retry-ms:60000}")
    @Transactional
    public void processar() {
        for (AgendaSyncPendente pendente : pendencias.findTop100ByOrderByIdAsc()) {
            try {
                sessoes.sincronizarAgenda(pendente.getSessao());
                pendencias.delete(pendente);
            } catch (RuntimeException ex) {
                pendente.registrarFalha();
                pendencias.save(pendente);
            }
        }
    }
}
