package com.kawz.tattoo.infrastructure.security;

/**
 * Principal colocado no SecurityContext pelo {@link JwtAuthenticationFilter}.
 * Carrega o id e o e-mail já validados no token, evitando uma consulta ao banco
 * por requisição só para descobrir quem é o dono do recurso.
 */
public record TatuadorPrincipal(Integer id, String email) {
}
