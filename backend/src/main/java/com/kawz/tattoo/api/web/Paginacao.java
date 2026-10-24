package com.kawz.tattoo.api.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Paginação padrão das listagens: pagina >= 0, tamanho 1..100 (padrão 20),
 * ordenação por id para páginas estáveis.
 */
public final class Paginacao {
    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    private Paginacao() {}

    public static Pageable de(int pagina, int tamanho) {
        return PageRequest.of(Math.max(0, pagina),
                Math.min(TAMANHO_MAXIMO, Math.max(1, tamanho)),
                Sort.by(Sort.Direction.ASC, "id"));
    }

    /** Sem ordenação: queries nativas que já têm ORDER BY próprio (vw_contratos). */
    public static Pageable semOrdenacao(int pagina, int tamanho) {
        return PageRequest.of(Math.max(0, pagina),
                Math.min(TAMANHO_MAXIMO, Math.max(1, tamanho)));
    }
}
