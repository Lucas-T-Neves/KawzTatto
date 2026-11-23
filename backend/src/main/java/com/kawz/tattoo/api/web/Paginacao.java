package com.kawz.tattoo.api.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Paginação padrão das listagens: pagina >= 0, tamanho 1..200 (padrão 20),
 * ordenação por id para páginas estáveis.
 *
 * <p>O limite é 200 porque o frontend solicita {@code tamanho=200} nas telas de
 * agenda e financeiro. Limitar em 100 truncaria dados silenciosamente.</p>
 */
public final class Paginacao {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 200;

    private Paginacao() {}

    // Pageable simplifica o transporte do índice, tamanho e ordenação da página
    // até o repository, que usa esses dados para gerar LIMIT/OFFSET no banco.
    public static Pageable de(int pagina, int tamanho) {
        return PageRequest.of(Math.max(0, pagina),
                Math.min(TAMANHO_MAXIMO, Math.max(1, tamanho)),
                Sort.by(Sort.Direction.ASC, "id"));
    }

    /** Sem ordenação: queries nativas que já têm ORDER BY próprio (vw_contratos). */
    // Esta variação mantém a mesma abstração de paginação, mas deixa a ordenação
    // para a consulta nativa que já define seu próprio ORDER BY.
    public static Pageable semOrdenacao(int pagina, int tamanho) {
        return PageRequest.of(Math.max(0, pagina),
                Math.min(TAMANHO_MAXIMO, Math.max(1, tamanho)));
    }
}
