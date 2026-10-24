package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimentacao_financeira")
public class MovimentacaoFinanceira extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
    @Column(name = "natureza_transacao", length = 45)
    private String naturezaTransacao;
    @Column(length = 80)
    private String categoria;
    @Column(name = "data_ocorrencia")
    private LocalDate dataOcorrencia;
    @Column(name = "valor_monetario", precision = 10, scale = 2)
    private BigDecimal valorMonetario;
    @Column(name = "forma_pagamento", length = 45)
    private String formaPagamento;
    @Column(name = "status_pagamento", length = 40)
    private String statusPagamento;

    protected MovimentacaoFinanceira() {}
    public MovimentacaoFinanceira(Projeto projeto, String naturezaTransacao, String categoria,
                                  LocalDate dataOcorrencia, BigDecimal valorMonetario,
                                  String formaPagamento, String statusPagamento) {
        this.projeto = projeto;
        atualizar(naturezaTransacao, categoria, dataOcorrencia, valorMonetario, formaPagamento, statusPagamento);
    }

    public void atualizar(String naturezaTransacao, String categoria, LocalDate dataOcorrencia,
                          BigDecimal valorMonetario, String formaPagamento, String statusPagamento) {
        this.naturezaTransacao = naturezaTransacao;
        this.categoria = categoria;
        this.dataOcorrencia = dataOcorrencia;
        this.valorMonetario = valorMonetario;
        this.formaPagamento = formaPagamento;
        this.statusPagamento = statusPagamento;
    }
    public void registrar() {
        if (dataOcorrencia == null) dataOcorrencia = LocalDate.now();
        if (statusPagamento == null) statusPagamento = "PENDENTE";
    }
    public void vincularProjeto(Projeto projeto) { this.projeto = projeto; }

    public Projeto getProjeto() { return projeto; }
    public String getNaturezaTransacao() { return naturezaTransacao; }
    public String getCategoria() { return categoria; }
    public LocalDate getDataOcorrencia() { return dataOcorrencia; }
    public BigDecimal getValorMonetario() { return valorMonetario; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getStatusPagamento() { return statusPagamento; }
}
