package com.teste.apis.exception;

import java.util.NoSuchElementException;

/** Recurso nao encontrado: vira 404. */
public class RecursoNaoEncontradoException extends NoSuchElementException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
