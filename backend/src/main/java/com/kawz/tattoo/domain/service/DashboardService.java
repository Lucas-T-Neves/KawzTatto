package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.DashboardResponse;
import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoResumoResponse;
import com.kawz.tattoo.domain.model.FollowUp;
import com.kawz.tattoo.domain.repository.FollowUpRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import com.kawz.tattoo.domain.repository.SessaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class DashboardService {
    private final ProjetoRepository projetos;
    private final SessaoRepository sessoes;
    private final FollowUpRepository followUps;
    private final MovimentacaoService movimentacoes;

    public DashboardService(ProjetoRepository projetos, SessaoRepository sessoes,
                            FollowUpRepository followUps, MovimentacaoService movimentacoes) {
        this.projetos = projetos;
        this.sessoes = sessoes;
        this.followUps = followUps;
        this.movimentacoes = movimentacoes;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumir(YearMonth mes) {
        LocalDate hoje = LocalDate.now();
        long projetosAtivos = projetos.findAll().stream()
                .filter(p -> p.getStatusProjeto() == null
                        || (!p.getStatusProjeto().equalsIgnoreCase("CANCELADO")
                        && !p.getStatusProjeto().equalsIgnoreCase("FINALIZADO")
                        && !p.getStatusProjeto().equalsIgnoreCase("CONCLUIDO")))
                .count();
        long sessoesHoje = sessoes.findAll().stream()
                .filter(s -> s.getDataHorario() != null && s.getDataHorario().toLocalDate().equals(hoje))
                .filter(s -> s.getStatusSessao() == null || !s.getStatusSessao().equalsIgnoreCase("CANCELADA"))
                .count();
        long followUpsPendentes = followUps.findAll().stream()
                .filter(this::pendente)
                .count();
        MovimentacaoResumoResponse financeiro = movimentacoes.resumo(mes.toString());
        return new DashboardResponse(mes.toString(), projetosAtivos, sessoesHoje, followUpsPendentes,
                financeiro.receita(), financeiro.despesas(), financeiro.saldo(), financeiro.pendencias());
    }

    private boolean pendente(FollowUp followUp) {
        String status = followUp.getStatusContato();
        return status == null || (!status.equalsIgnoreCase("REALIZADO")
                && !status.equalsIgnoreCase("CONCLUIDO")
                && !status.equalsIgnoreCase("CANCELADO"));
    }
}
