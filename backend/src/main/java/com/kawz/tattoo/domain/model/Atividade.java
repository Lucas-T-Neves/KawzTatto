package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "atividade")
public class Atividade extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Tatuador tatuador;
    @Column(length = 45)
    private String descricao;
    @Column(length = 45)
    private String tipo;

    protected Atividade() {}
    public Atividade(Tatuador tatuador, String descricao, String tipo) {
        this.tatuador = tatuador;
        this.descricao = descricao;
        this.tipo = tipo;
    }

    public void alterarDescricao(String novaDescricao) { this.descricao = novaDescricao; }
    public void atualizar(String descricao, String tipo) { this.descricao = descricao; this.tipo = tipo; }
    public void vincularTatuador(Tatuador tatuador) { this.tatuador = tatuador; }
    public Tatuador getTatuador() { return tatuador; }
    public String getDescricao() { return descricao; }
    public String getTipo() { return tipo; }
}
