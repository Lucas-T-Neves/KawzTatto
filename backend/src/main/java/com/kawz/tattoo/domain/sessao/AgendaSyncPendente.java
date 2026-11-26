package com.kawz.tattoo.domain.sessao;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agenda_sync_pendente")
public class AgendaSyncPendente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_id", nullable = false)
    private Sessao sessao;
    @Column(nullable = false, length = 30)
    private String operacao;
    @Column(nullable = false)
    private int tentativas;
    @Column(name = "ultima_tentativa")
    private Instant ultimaTentativa;

    protected AgendaSyncPendente() {}
    public AgendaSyncPendente(Sessao sessao, String operacao) {
        this.sessao = sessao;
        this.operacao = operacao;
    }
    public void registrarFalha() { tentativas++; ultimaTentativa = Instant.now(); }
    public Integer getId() { return id; }
    public Sessao getSessao() { return sessao; }
    public String getOperacao() { return operacao; }
    public int getTentativas() { return tentativas; }
}
