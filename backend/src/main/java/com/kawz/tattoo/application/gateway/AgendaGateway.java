package com.kawz.tattoo.application.gateway;

import java.time.LocalDateTime;

/**
 * Porta de agenda externa. A aplicação depende desta capacidade, não do Nylas:
 * o fornecedor atual é a implementação
 * {@code infrastructure.integration.NylasAgendaGateway}.
 *
 * <p>Política de falha: criar/atualizar evento bloqueia a operação (a transação
 * da sessão é desfeita e a API responde 502); remover evento é "melhor esforço"
 * (a sessão já saiu do banco e a falha é apenas registrada em log). Quando a
 * integração está desligada, a sessão é salva normalmente sem evento.</p>
 */
public interface AgendaGateway {

    /** Indica se há credenciais configuradas para a agenda externa. */
    boolean ativo();

    /** Cria o evento e devolve o id externo (null quando a integração está desligada). */
    String criarEvento(String titulo, String descricao, String local, LocalDateTime inicio);

    void atualizarEvento(String eventoId, String titulo, String descricao, String local, LocalDateTime inicio);

    void removerEvento(String eventoId);
}
