package com.kawz.tattoo.infrastructure.integration;

import com.kawz.tattoo.application.gateway.AgendaGateway;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador da porta de agenda sobre a API do Nylas.
 * A aplicação depende de {@link AgendaGateway}; este adaptador é o único ponto
 * que conhece o fornecedor.
 */
@Component
public class NylasAgendaGateway implements AgendaGateway {

    private final NylasCalendarClient nylas;

    public NylasAgendaGateway(NylasCalendarClient nylas) {
        this.nylas = nylas;
    }

    @Override
    public boolean ativo() {
        return nylas.ativo();
    }

    @Override
    public String criarEvento(String titulo, String descricao, String local, LocalDateTime inicio) {
        return nylas.criarEvento(titulo, descricao, local, inicio);
    }

    @Override
    public String criarEvento(String titulo, String descricao, String local, LocalDateTime inicio, Integer duracaoHoras) {
        return nylas.criarEvento(titulo, descricao, local, inicio, duracaoHoras);
    }

    @Override
    public void atualizarEvento(String eventoId, String titulo, String descricao, String local, LocalDateTime inicio) {
        nylas.atualizarEvento(eventoId, titulo, descricao, local, inicio);
    }

    @Override
    public void atualizarEvento(String eventoId, String titulo, String descricao, String local, LocalDateTime inicio, Integer duracaoHoras) {
        nylas.atualizarEvento(eventoId, titulo, descricao, local, inicio, duracaoHoras);
    }

    @Override
    public void removerEvento(String eventoId) {
        nylas.removerEvento(eventoId);
    }
}
