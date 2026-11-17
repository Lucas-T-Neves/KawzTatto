package com.kawz.tattoo.domain.dashboard;

import com.kawz.tattoo.domain.movimentacao.MovimentacaoService;


import com.kawz.tattoo.api.dashboard.dto.DashboardResponse;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResumoResponse;
import com.kawz.tattoo.domain.followup.FollowUp;
import com.kawz.tattoo.domain.followup.FollowUpRepository;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import com.kawz.tattoo.domain.sessao.SessaoRepository;
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
