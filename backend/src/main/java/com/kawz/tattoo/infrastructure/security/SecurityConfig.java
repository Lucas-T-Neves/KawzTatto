package com.kawz.tattoo.infrastructure.security;

import com.kawz.tattoo.domain.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Segurança stateless com JWT:
 * - abertos: POST /api/v1/tatuadores (registro), POST /autenticacao e docs;
 * - todo o resto exige "Authorization: Bearer &lt;token&gt;" (401 RFC 9457 sem token).
 *
 * <p>Autenticação não é autorização: o filtro identifica o tatuador (id + e-mail),
 * e cada caso de uso filtra os recursos pelo proprietário autenticado.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/tatuadores").permitAll()
                        .requestMatchers("/api/v1/tatuadores/autenticacao").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new ProblemDetailEntryPoint()))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * CORS restrito às origens configuradas em {@code kawz.cors.origens}
     * (variável CORS_ORIGENS). Sem origens configuradas não há liberação de CORS:
     * em desenvolvimento o Vite usa proxy same-origin e em produção o esperado é
     * publicar frontend e backend na mesma origem.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${kawz.cors.origens:}") String origens) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        List<String> permitidas = Arrays.stream(origens.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isBlank())
                .toList();
        if (permitidas.isEmpty()) return source;
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(permitidas);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracao.setMaxAge(3600L);
        source.registerCorsConfiguration("/api/**", configuracao);
        return source;
    }

    /** 401 no padrão RFC 9457 para requisições sem/com token inválido. */
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
