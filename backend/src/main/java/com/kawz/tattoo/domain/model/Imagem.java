package com.kawz.tattoo.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "imagem")
// Chave primária nomeada "imagem" conforme o DER (`imagem INT AUTO_INCREMENT`)
@AttributeOverride(name = "id", column = @Column(name = "imagem"))
public class Imagem extends BaseEntity {
    @Column(name = "publicId", length = 255)
    private String publicId;
    @Column(length = 1000)
    private String url;
    @Column(length = 20)
    private String formato;
    @Column(length = 100)
    private String bytes;
    // O DER usa o identificador `largura/altura`; aspas escapadas para o H2
    @Column(name = "\"largura/altura\"")
    private Integer larguraAltura;
    @Column(name = "criadoEm")
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Projeto projeto;

    protected Imagem() {}

    public Imagem(Projeto projeto, String publicId, String url, String formato,
                  String bytes, Integer larguraAltura) {
        this.projeto = projeto;
        this.publicId = publicId;
        this.url = url;
        this.formato = formato;
        this.bytes = bytes;
        this.larguraAltura = larguraAltura;
    }

    @PrePersist
    void prePersist() { if (criadoEm == null) criadoEm = LocalDateTime.now(); }

    public void atualizar(String publicId, String url, String formato, String bytes, Integer larguraAltura) {
        this.publicId = publicId;
        this.url = url;
        this.formato = formato;
        this.bytes = bytes;
        this.larguraAltura = larguraAltura;
    }

    public void vincularProjeto(Projeto projeto) { this.projeto = projeto; }

    public String obterPublicId() { return publicId; }

    public Projeto getProjeto() { return projeto; }
    public String getPublicId() { return publicId; }
    public String getUrl() { return url; }
    public String getFormato() { return formato; }
    public String getBytes() { return bytes; }
    public Integer getLarguraAltura() { return larguraAltura; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
