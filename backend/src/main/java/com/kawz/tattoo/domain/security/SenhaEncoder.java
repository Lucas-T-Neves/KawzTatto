package com.kawz.tattoo.domain.security;

public interface SenhaEncoder {
    String codificar(String senha);
    boolean corresponde(String senhaInformada, String senhaCodificada);
}
