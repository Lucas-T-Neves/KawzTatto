package com.kawz.tattoo.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "projeto")
public class Projeto extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Tatuador tatuador;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Column(length = 45)
    private String descricao;
    @Column(name = "status_projeto", length = 45)
    private String statusProjeto;
    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;
    @Column(name = "valor_sinal", precision = 10, scale = 2)
    private BigDecimal valorSinal;
    @Column(name = "quantidade_sessoes")
    private Integer quantidadeSessoes;
    @Column(name = "data_validade")
    private LocalDate dataValidade;

    // FKs do lado do projeto, conforme o script: contrato_id e
    // caracteristicas_projeto_id ambos NOT NULL + UNIQUE em projeto
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private Contrato contrato;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "caracteristicas_projeto_id", nullable = false, unique = true)
    private CaracteristicasProjeto caracteristicas;

    protected Projeto() {}

    public Projeto(Tatuador tatuador, Cliente cliente, String descricao, String statusProjeto,
                   BigDecimal valorTotal, BigDecimal valorSinal, Integer quantidadeSessoes, LocalDate dataValidade) {
        this.tatuador = tatuador;
        this.cliente = cliente;
        atualizar(descricao, statusProjeto, valorTotal, valorSinal, quantidadeSessoes, dataValidade);
        this.contrato = new Contrato();
        this.caracteristicas = new CaracteristicasProjeto();
    }

    public void atualizar(String descricao, String statusProjeto, BigDecimal valorTotal, BigDecimal valorSinal,
                          Integer quantidadeSessoes, LocalDate dataValidade) {
        this.descricao = descricao;
        this.statusProjeto = statusProjeto;
        this.valorTotal = valorTotal;
        this.valorSinal = valorSinal;
        this.quantidadeSessoes = quantidadeSessoes;
        this.dataValidade = dataValidade;
    }

    /**
     * Saldo a receber: valor total menos o que já foi recebido (movimentações
     * de ENTRADA pagas). Alinhado à regra da view vw_contratos, que marca o
     * sinal como pago pelas movimentações — não pelo valor_sinal do contrato.
     */
    public BigDecimal calcularSaldo(BigDecimal recebido) {
        if (valorTotal == null) return null;
        return valorTotal.subtract(recebido == null ? BigDecimal.ZERO : recebido);
    }

    public void alterarStatus(String novoStatus) { this.statusProjeto = novoStatus; }
    public void vincular(Tatuador tatuador, Cliente cliente) {
        this.tatuador = tatuador;
        this.cliente = cliente;
    }

    public Tatuador getTatuador() { return tatuador; }
    public Cliente getCliente() { return cliente; }
    public String getDescricao() { return descricao; }
    public String getStatusProjeto() { return statusProjeto; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getValorSinal() { return valorSinal; }
    public Integer getQuantidadeSessoes() { return quantidadeSessoes; }
    public LocalDate getDataValidade() { return dataValidade; }
    public Contrato getContrato() { return contrato; }
    public CaracteristicasProjeto getCaracteristicas() { return caracteristicas; }
}
