package com.kawz.tattoo.infrastructure.security;

import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.exception.NaoAutenticadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementação de {@link TatuadorAutenticado} sobre o SecurityContext.
 *
 * <p>Se não houver autenticação (ou o principal não for um tatuador), a operação
 * falha com 401 em vez de assumir um proprietário padrão.</p>
 */
@Component
public class ContextoSegurancaTatuadorAutenticado implements TatuadorAutenticado {

    @Override
    public Integer id() {
        return principal().id();
    }

    @Override
    public String email() {
        return principal().email();
    }

    private TatuadorPrincipal principal() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || !(autenticacao.getPrincipal() instanceof TatuadorPrincipal tatuador)) {
            throw new NaoAutenticadoException("Requisição sem tatuador autenticado");
        }
        return tatuador;
    }
}
