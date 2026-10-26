package com.kawz.tattoo.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "atividade")
public class Atividade extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Tatuador tatuador;
    @Column(length = 45) private String descricao;
    @Column(length = 45) private String tipo;
    @Column(length = 120) private String titulo;
    @Column(name = "data_atividade") private LocalDate data;
    @Column(name = "hora_inicio") private LocalTime inicio;
    @Column(name = "hora_termino") private LocalTime termino;
    @Column(length = 255) private String local;
    @Column(length = 2000) private String observacoes;
    protected Atividade() {}
    public Atividade(Tatuador tatuador, String descricao, String tipo) {
        this(tatuador, descricao, tipo, descricao, null, null, null, null, null);
    }
    public Atividade(Tatuador tatuador, String descricao, String tipo, String titulo,
                     LocalDate data, LocalTime inicio, LocalTime termino, String local, String observacoes) {
        this.tatuador=tatuador; this.descricao=descricao; this.tipo=tipo; this.titulo=titulo;
        this.data=data; this.inicio=inicio; this.termino=termino; this.local=local; this.observacoes=observacoes;
    }
    public void alterarDescricao(String novaDescricao) { this.descricao=novaDescricao; }
    public void atualizar(String descricao, String tipo) { atualizar(descricao,tipo,descricao,null,null,null,null,null); }
    public void atualizar(String descricao, String tipo, String titulo, LocalDate data, LocalTime inicio,
                          LocalTime termino, String local, String observacoes) {
        this.descricao=descricao; this.tipo=tipo; this.titulo=titulo; this.data=data; this.inicio=inicio;
        this.termino=termino; this.local=local; this.observacoes=observacoes;
    }
    public void vincularTatuador(Tatuador tatuador) { this.tatuador=tatuador; }
    public Tatuador getTatuador(){return tatuador;} public String getDescricao(){return descricao;}
    public String getTipo(){return tipo;} public String getTitulo(){return titulo;} public LocalDate getData(){return data;}
    public LocalTime getInicio(){return inicio;} public LocalTime getTermino(){return termino;}
    public String getLocal(){return local;} public String getObservacoes(){return observacoes;}
}
