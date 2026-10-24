package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "follow_up")
public class FollowUp extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Column(name = "tipo_contato", length = 45)
    private String tipoContato;
    @Column(name = "status_contato", length = 45)
    private String statusContato;
    @Column(name = "data_prevista")
    private LocalDate dataPrevista;
    @Column(name = "data_realizacao")
    private LocalDate dataRealizacao;
    @Column(name = "descricao_observacao", columnDefinition = "text")
    private String descricaoObservacao;
    @Column(name = "proxima_acao", length = 45)
    private String proximaAcao;

    protected FollowUp() {}

    public FollowUp(Cliente cliente, String tipoContato, String statusContato, LocalDate dataPrevista,
                    LocalDate dataRealizacao, String descricaoObservacao, String proximaAcao) {
        this.cliente = cliente;
        atualizar(tipoContato, statusContato, dataPrevista, dataRealizacao, descricaoObservacao, proximaAcao);
    }

    public void atualizar(String tipoContato, String statusContato, LocalDate dataPrevista,
                          LocalDate dataRealizacao, String descricaoObservacao, String proximaAcao) {
        this.tipoContato = tipoContato;
        this.statusContato = statusContato;
        this.dataPrevista = dataPrevista;
        this.dataRealizacao = dataRealizacao;
        this.descricaoObservacao = descricaoObservacao;
        this.proximaAcao = proximaAcao;
    }

    public void vincularCliente(Cliente cliente) { this.cliente = cliente; }

    public void reagendar(LocalDate novaData) {
        this.dataPrevista = novaData;
        this.statusContato = "AGENDADO";
    }

    public void concluir() {
        this.statusContato = "CONCLUIDO";
        this.dataRealizacao = LocalDate.now();
    }

    public Cliente getCliente() { return cliente; }
    public String getTipoContato() { return tipoContato; }
    public String getStatusContato() { return statusContato; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public LocalDate getDataRealizacao() { return dataRealizacao; }
    public String getDescricaoObservacao() { return descricaoObservacao; }
    public String getProximaAcao() { return proximaAcao; }
}
