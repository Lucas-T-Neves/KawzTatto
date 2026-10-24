package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "contrato")
public class Contrato extends BaseEntity {
    @Column(name = "limite_alteracoes_arte", length = 45)
    private String limiteAlteracoesArte;
    @Column(name = "tolerancia_atraso", length = 45)
    private String toleranciaAtraso;
    @Column(name = "uso_imagem", length = 45)
    private String usoImagem;
    @Column(name = "data_assinatura")
    private LocalDate dataAssinatura;

    protected Contrato() {}

    public void atualizar(String limiteAlteracoesArte, String toleranciaAtraso, String usoImagem) {
        this.limiteAlteracoesArte = limiteAlteracoesArte;
        this.toleranciaAtraso = toleranciaAtraso;
        this.usoImagem = usoImagem;
    }

    public void registrarAssinatura(LocalDate data) {
        this.dataAssinatura = data;
    }

    public String getLimiteAlteracoesArte() { return limiteAlteracoesArte; }
    public String getToleranciaAtraso() { return toleranciaAtraso; }
    public String getUsoImagem() { return usoImagem; }
    public LocalDate getDataAssinatura() { return dataAssinatura; }
}
