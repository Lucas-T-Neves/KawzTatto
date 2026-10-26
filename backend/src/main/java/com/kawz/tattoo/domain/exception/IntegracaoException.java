package com.kawz.tattoo.domain.exception;

/** Falha ou ausência de configuração de um serviço externo (Cloudinary, APITemplate, Nylas). Vira HTTP 502. */
public class IntegracaoException extends RuntimeException {
    public IntegracaoException(String message) { super(message); }
    public IntegracaoException(String message, Throwable cause) { super(message, cause); }
}
