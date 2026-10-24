package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "caracteristicas_projeto")
public class CaracteristicasProjeto extends BaseEntity {
    @Column(name = "ideia_expectativa", columnDefinition = "text")
    private String ideiaExpectativa;
    @Column(name = "estilo_arte", length = 45)
    private String estiloArte;
    @Column(name = "localizacao_corpo", length = 45)
    private String localizacaoCorpo;
    @Column(length = 45)
    private String dificuldade;
    @Column(length = 45)
    private String cores;
    @Column(length = 45)
    private String complexidade;

    protected CaracteristicasProjeto() {}

    public void atualizarCaracteristicas(String ideiaExpectativa, String estiloArte, String localizacaoCorpo,
                                         String dificuldade, String cores, String complexidade) {
        this.ideiaExpectativa = ideiaExpectativa;
        this.estiloArte = estiloArte;
        this.localizacaoCorpo = localizacaoCorpo;
        this.dificuldade = dificuldade;
        this.cores = cores;
        this.complexidade = complexidade;
    }

    public String getIdeiaExpectativa() { return ideiaExpectativa; }
    public String getEstiloArte() { return estiloArte; }
    public String getLocalizacaoCorpo() { return localizacaoCorpo; }
    public String getDificuldade() { return dificuldade; }
    public String getCores() { return cores; }
    public String getComplexidade() { return complexidade; }
}
