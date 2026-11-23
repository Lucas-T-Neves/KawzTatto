package com.kawz.tattoo.domain.security;

import com.kawz.tattoo.domain.tatuador.Tatuador;
import com.kawz.tattoo.infrastructure.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Emite e valida tokens JWT (HS256). O segredo e a validade vêm de
 * {@link JwtProperties} (kawz.jwt.segredo / kawz.jwt.validade-minutos),
 * já validados no start da aplicação.
 */
@Service
public class JwtService {

    public record TokenEmissao(String token, LocalDateTime expiraEm) {}

    /** Identidade carregada no token: o id é o proprietário dos recursos. */
    public record TatuadorToken(Integer id, String email) {}

    private final SecretKey chave;
    private final long validadeMinutos;

    public JwtService(JwtProperties propriedades) {
        this.chave = Keys.hmacShaKeyFor(propriedades.segredo().getBytes(StandardCharsets.UTF_8));
        this.validadeMinutos = propriedades.validadeMinutos();
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

    /**
     * Devolve a identidade (id + e-mail) do token ou vazio se a assinatura for
     * inválida, o token estiver expirado ou não houver claim de id.
     */
    public Optional<TatuadorToken> extrair(String token) {
        try {
            var claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            String email = claims.getSubject();
            Object id = claims.get("id");
            if (email == null || id == null) return Optional.empty();
            Integer tatuadorId = id instanceof Number numero ? numero.intValue() : null;
            if (tatuadorId == null) return Optional.empty();
            return Optional.of(new TatuadorToken(tatuadorId, email));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
