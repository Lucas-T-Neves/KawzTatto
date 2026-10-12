package com.teste.apis.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** O filtro de headers de seguranca precisa estar em todas as respostas. */
class SecurityHeadersFilterTest {

    @Test
    @DisplayName("adiciona headers de protecao e segue a cadeia")
    void adicionaHeaders() throws Exception {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/contratos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean[] seguiuCadeia = {false};
        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                seguiuCadeia[0] = true;
            }
        };

        filter.doFilter(request, response, chain);

        assertThat(seguiuCadeia[0]).isTrue();
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(response.getHeader("Content-Security-Policy")).contains("frame-ancestors 'none'");
        assertThat(response.getHeader("Strict-Transport-Security")).contains("max-age=31536000");
    }
}
