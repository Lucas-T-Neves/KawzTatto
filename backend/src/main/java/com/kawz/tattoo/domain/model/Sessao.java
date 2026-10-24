package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessao")
public class Sessao extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;
    @Column(name = "data_horario")
    private LocalDateTime dataHorario;
    @Column(name = "local_atendimento", length = 45)
    private String localAtendimento;
    @Column(name = "status_sessao", length = 45)
    private String statusSessao;
    @Column(name = "observacoes_sessao", length = 45)
    private String observacoesSessao;

    protected Sessao() {}
    public Sessao(Projeto projeto, LocalDateTime dataHorario, String localAtendimento,
                  String statusSessao, String observacoesSessao) {
        this.projeto = projeto;
        atualizar(dataHorario, localAtendimento, statusSessao, observacoesSessao);
    }

    public void atualizar(LocalDateTime dataHorario, String localAtendimento,
                          String statusSessao, String observacoesSessao) {
        this.dataHorario = dataHorario;
        this.localAtendimento = localAtendimento;
        this.statusSessao = statusSessao;
        this.observacoesSessao = observacoesSessao;
    }
    public void remarcar(LocalDateTime novoHorario) {
        this.dataHorario = novoHorario;
        this.statusSessao = "REMARCADA";
    }
    public void vincularProjeto(Projeto projeto) { this.projeto = projeto; }

    public Projeto getProjeto() { return projeto; }
    public LocalDateTime getDataHorario() { return dataHorario; }
    public String getLocalAtendimento() { return localAtendimento; }
    public String getStatusSessao() { return statusSessao; }
    public String getObservacoesSessao() { return observacoesSessao; }
}
