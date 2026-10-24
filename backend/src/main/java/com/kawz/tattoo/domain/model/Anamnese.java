package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "anamnese")
public class Anamnese extends BaseEntity {
    // Correção sobre o DER original: 1 cliente -> 1 ou muitas anamneses (FK na anamnese)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "dados_saude", length = 45)
    private String dadosSaude;
    @Column(name = "preparacao_tatuagem", length = 45)
    private String preparacaoTatuagem;

    protected Anamnese() {}
    public Anamnese(Cliente cliente, String dadosSaude, String preparacaoTatuagem) {
        this.cliente = cliente;
        atualizarInformacoes(dadosSaude, preparacaoTatuagem);
    }

    public void atualizarInformacoes(String dadosSaude, String preparacaoTatuagem) {
        this.dadosSaude = dadosSaude;
        this.preparacaoTatuagem = preparacaoTatuagem;
    }

    public Cliente getCliente() { return cliente; }
    public String getDadosSaude() { return dadosSaude; }
    public String getPreparacaoTatuagem() { return preparacaoTatuagem; }
}
