package com.kawz.tattoo.domain.tatuador;

import com.kawz.tattoo.domain.shared.BaseEntity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import com.kawz.tattoo.domain.security.SenhaEncoder;

@Entity
@Table(name = "tatuador")
public class Tatuador extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    @Column(name = "logo_public_id", length = 255)
    private String logoPublicId;

    protected Tatuador() {}

    public Tatuador(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public boolean autenticar(String senhaInformada, SenhaEncoder encoder) {
        return encoder.corresponde(senhaInformada, senha);
    }

    public void atualizar(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public void alterarSenha(String senhaCodificada) {
        this.senha = senhaCodificada;
    }

    /** Estrutura preparada para logo global; nenhum logo é escolhido por padrão. */
    public void registrarLogo(String url, String publicId) {
        this.logoUrl = url;
        this.logoPublicId = publicId;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getLogoUrl() { return logoUrl; }
    public String getLogoPublicId() { return logoPublicId; }
}
