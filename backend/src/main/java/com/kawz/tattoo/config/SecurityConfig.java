package com.kawz.tattoo.config;

import com.kawz.tattoo.domain.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

/**
 * Segurança stateless com JWT:
 * - abertos: POST /api/v1/tatuadores (registro), POST /autenticacao e docs;
 * - todo o resto exige "Authorization: Bearer &lt;token&gt;" (401 RFC 9457 sem token).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/tatuadores").permitAll()
                        .requestMatchers("/api/v1/tatuadores/autenticacao").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new ProblemDetailEntryPoint()))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** 401 no padrão RFC 9457 para requisições sem/ com token inválido. */
    static class ProblemDetailEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            String json = "{"
                    + "\"type\":\"https://kawz-tattoo-api.local/erros/nao-autenticado\","
                    + "\"title\":\"Não autenticado\","
                    + "\"status\":401,"
                    + "\"detail\":\"Token ausente, inválido ou expirado\","
                    + "\"instance\":\"" + request.getRequestURI() + "\","
                    + "\"timestamp\":\"" + LocalDateTime.now() + "\""
                    + "}";
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        }
    }
}
