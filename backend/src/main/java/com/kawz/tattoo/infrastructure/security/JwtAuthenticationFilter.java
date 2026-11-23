package com.kawz.tattoo.infrastructure.security;

import com.kawz.tattoo.domain.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lê o header "Authorization: Bearer &lt;token&gt;" e, se o JWT for válido, popula o
 * SecurityContext com um {@link TatuadorPrincipal} (id + e-mail do tatuador).
 *
 * <p>O id no principal é a única fonte de propriedade dos recursos: nenhum
 * tatuadorId enviado no JSON ou na URL é usado para autorizar operações.</p>
 *
 * <p>Não é @Component de propósito: só é registrado dentro da SecurityFilterChain
 * (evita registro duplo como filtro de servlet).</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            jwtService.extrair(header.substring(7).trim())
                    .filter(token -> SecurityContextHolder.getContext().getAuthentication() == null)
                    .ifPresent(token -> {
                        var principal = new TatuadorPrincipal(token.id(), token.email());
                        var authentication = new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_TATUADOR")));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
        chain.doFilter(request, response);
    }
}
