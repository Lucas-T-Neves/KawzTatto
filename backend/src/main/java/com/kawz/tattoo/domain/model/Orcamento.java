package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orcamento")
public class Orcamento extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(length = 150)
    private String nome;

    @Column(name = "data_orcamento")
    private LocalDate dataOrcamento;

    @Column(length = 45)
    private String status;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_sinal", precision = 10, scale = 2)
    private BigDecimal valorSinal;

    @Column(name = "quantidade_sessoes")
    private Integer quantidadeSessoes;

    @Column(name = "forma_pagamento", length = 45)
    private String formaPagamento;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "proposta_aceita")
    private Boolean propostaAceita;

    protected Orcamento() {}

    public Orcamento(Projeto projeto, Cliente cliente, String nome, LocalDate dataOrcamento, String status,
                     BigDecimal valorTotal, BigDecimal valorSinal, Integer quantidadeSessoes,
                     String formaPagamento, LocalDate dataValidade, Boolean propostaAceita) {
        this.projeto = projeto;
        this.cliente = cliente;
        atualizar(nome, dataOrcamento, status, valorTotal, valorSinal, quantidadeSessoes,
                formaPagamento, dataValidade, propostaAceita);
    }

    @PrePersist
    void prePersist() {
        if (dataOrcamento == null) dataOrcamento = LocalDate.now();
        if (status == null || status.isBlank()) status = "Rascunho";
        if (propostaAceita == null) propostaAceita = Boolean.FALSE;
    }

    public void atualizar(String nome, LocalDate dataOrcamento, String status,
                         BigDecimal valorTotal, BigDecimal valorSinal, Integer quantidadeSessoes,
                         String formaPagamento, LocalDate dataValidade, Boolean propostaAceita) {
        this.nome = nome;
        this.dataOrcamento = dataOrcamento;
        this.status = status;
        this.valorTotal = valorTotal;
        this.valorSinal = valorSinal;
        this.quantidadeSessoes = quantidadeSessoes;
        this.formaPagamento = formaPagamento;
        this.dataValidade = dataValidade;
        this.propostaAceita = propostaAceita;
    }

    public void vincularProjeto(Projeto projeto) { this.projeto = projeto; }
    public void vincularCliente(Cliente cliente) { this.cliente = cliente; }

    public BigDecimal valorSessaoEstimado() {
        if (valorTotal == null || quantidadeSessoes == null || quantidadeSessoes <= 0) return null;
        return valorTotal.divide(BigDecimal.valueOf(quantidadeSessoes), 2, java.math.RoundingMode.HALF_UP);
    }

    public Projeto getProjeto() { return projeto; }
    public Cliente getCliente() { return cliente; }
    public String getNome() { return nome; }
    public LocalDate getDataOrcamento() { return dataOrcamento; }
    public String getStatus() { return status; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getValorSinal() { return valorSinal; }
    public Integer getQuantidadeSessoes() { return quantidadeSessoes; }
    public String getFormaPagamento() { return formaPagamento; }
    public LocalDate getDataValidade() { return dataValidade; }
    public Boolean getPropostaAceita() { return propostaAceita; }
}
