package com.kawz.tattoo.application.security;

/**
 * Porta de leitura da identidade autenticada.
 *
 * <p>Os controllers obtêm o tatuador autenticado por esta interface e passam o id
 * explicitamente para os casos de uso. Assim a camada de aplicação não depende de
 * Spring Security e continua testável sem contexto HTTP.</p>
 */
public interface TatuadorAutenticado {

    /** Id do tatuador dono da requisição (vem do JWT, nunca do corpo/URL). */
    Integer id();

    /** E-mail do tatuador autenticado. */
    String email();
}
