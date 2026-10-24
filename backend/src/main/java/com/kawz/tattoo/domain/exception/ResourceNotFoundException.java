package com.kawz.tattoo.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " não encontrado(a): " + id);
    }
}
