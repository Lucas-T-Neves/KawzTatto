package com.kawz.tattoo.config;

import com.kawz.tattoo.domain.security.SenhaEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class Pbkdf2SenhaEncoder implements SenhaEncoder {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 210_000;
    private static final int TAMANHO_CHAVE = 256;
    private static final int TAMANHO_SALT = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String codificar(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        secureRandom.nextBytes(salt);
        byte[] hash = derivar(senha.toCharArray(), salt, ITERACOES);
        return "pbkdf2$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean corresponde(String senhaInformada, String senhaCodificada) {
        try {
            String[] partes = senhaCodificada.split("\\$");
            if (partes.length != 4 || !"pbkdf2".equals(partes[0])) return false;
            int iteracoes = Integer.parseInt(partes[1]);
            if (iteracoes < 10_000 || iteracoes > 1_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] obtido = derivar(senhaInformada.toCharArray(), salt, iteracoes);
            return MessageDigest.isEqual(esperado, obtido);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] derivar(char[] senha, byte[] salt, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_CHAVE);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Algoritmo de hash de senha indisponível", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
