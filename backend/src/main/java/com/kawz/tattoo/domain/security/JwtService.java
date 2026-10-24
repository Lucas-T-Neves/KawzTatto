package com.kawz.tattoo.domain.security;

import com.kawz.tattoo.domain.model.Tatuador;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Emite e valida tokens JWT (HS256). O segredo e a validade vêm da
 * configuração (kawz.jwt.segredo / kawz.jwt.validade-minutos).
 */
@Service
public class JwtService {
    public record TokenEmissao(String token, LocalDateTime expiraEm) {}

    private final SecretKey chave;
    private final long validadeMinutos;

    public JwtService(
            @Value("${kawz.jwt.segredo:kawz-tattoo-dev-secret-troque-em-producao-0123456789abcdef-xyz}")
            String segredo,
            @Value("${kawz.jwt.validade-minutos:480}") long validadeMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.validadeMinutos = validadeMinutos;
    }

    public TokenEmissao emitir(Tatuador tatuador) {
        Instant agora = Instant.now();
        Instant expira = agora.plusSeconds(validadeMinutos * 60);
        String token = Jwts.builder()
                .subject(tatuador.getEmail())
                .claim("id", tatuador.getId())
                .claim("nome", tatuador.getNome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expira))
                .signWith(chave)
                .compact();
        return new TokenEmissao(token, LocalDateTime.ofInstant(expira, ZoneId.systemDefault()));
    }

    /** Devolve o e-mail (subject) do token ou vazio se assinatura inválida/expirada. */
    public Optional<String> extrairEmail(String token) {
        try {
            var claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
