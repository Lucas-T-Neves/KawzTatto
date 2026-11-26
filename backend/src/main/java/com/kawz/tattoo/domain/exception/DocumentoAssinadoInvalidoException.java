package com.kawz.tattoo.domain.exception;

public class DocumentoAssinadoInvalidoException extends BusinessException {
    public DocumentoAssinadoInvalidoException(String message) { super(message); }
    public DocumentoAssinadoInvalidoException(String message, Throwable cause) { super(message); initCause(cause); }
}
