package com.kawz.tattoo.domain.exception;

/** Requisição sem identidade válida (token ausente, inválido ou fora do contexto). Vira HTTP 401. */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String message) { super(message); }
}
