package com.teste.apis.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Headers de seguranca em todas as respostas.
 *
 * <p>Motivacao: o diagnostico de 2026-10-10 mostrou a aplicacao respondendo sem
 * nenhum header de protecao — risco de MIME sniffing e clickjacking (o console
 * do H2, quando ligado, e especialmente tentador para iframe).</p>
 *
 * <p>CSP com 'unsafe-inline' de proposito: o Swagger UI (perfil local) usa
 * recursos inline. Em producao atras de TLS, trocar por nonce/hash.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        resp.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline'; "
                        + "script-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'");
        // so vale atras de TLS; em HTTP puro o navegador ignora
        resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        chain.doFilter(request, response);
    }
}
