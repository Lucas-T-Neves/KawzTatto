package com.kawz.tattoo.domain.dashboard;

import com.kawz.tattoo.api.dashboard.dto.DashboardResponse;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResumoResponse;
import com.kawz.tattoo.domain.followup.FollowUp;
import com.kawz.tattoo.domain.followup.FollowUpRepository;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoService;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import com.kawz.tattoo.domain.sessao.SessaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Indicadores do tatuador autenticado (nunca do estúdio inteiro). */
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
    public DashboardResponse resumir(YearMonth mes, Integer tatuadorId) {
        LocalDate hoje = LocalDate.now();
        // stream().filter().count() simplifica a seleção dos projetos ativos
        // e a contagem dos elementos que atendem à regra.
        long projetosAtivos = projetos.findByTatuadorIdOrderByIdDesc(tatuadorId).stream()
                .filter(p -> p.getStatusProjeto() == null
                        || (!p.getStatusProjeto().equalsIgnoreCase("CANCELADO")
                        && !p.getStatusProjeto().equalsIgnoreCase("FINALIZADO")
                        && !p.getStatusProjeto().equalsIgnoreCase("CONCLUIDO")))
                .count();
        long sessoesHoje = sessoes.findByDataHorarioAfterAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
                        hoje.atStartOfDay(), tatuadorId,
                        org.springframework.data.domain.PageRequest.of(0, 500))
                .stream()
                .filter(s -> s.getDataHorario().toLocalDate().equals(hoje))
                .filter(s -> s.getStatusSessao() == null || !s.getStatusSessao().equalsIgnoreCase("CANCELADA"))
                .count();
        // stream().filter().count() filtra os follow-ups pendentes e conta
        // somente os registros que satisfazem a condição.
        long followUpsPendentes = followUps.findByCliente_Tatuador_Id(tatuadorId).stream()
                .filter(this::pendente)
                .count();
        MovimentacaoResumoResponse financeiro = movimentacoes.resumo(mes.toString(), tatuadorId);
        return new DashboardResponse(mes.toString(), projetosAtivos, sessoesHoje, followUpsPendentes,
                financeiro.receita(), financeiro.despesas(), financeiro.saldo(), financeiro.pendencias(), alertas(hoje, tatuadorId));
    }

    private List<DashboardResponse.Alerta> alertas(LocalDate hoje, Integer tatuadorId) {
        LocalDateTime inicio = hoje.minusDays(30).atStartOfDay();
        var sessoesDoPeriodo = sessoes.findByDataHorarioAfterAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
                inicio, tatuadorId, org.springframework.data.domain.PageRequest.of(0, 500));
        List<DashboardResponse.Alerta> result = new ArrayList<>();
        for (var sessao : sessoesDoPeriodo) {
            if (sessao.getStatusSessao() == null || !sessao.getStatusSessao().equalsIgnoreCase("CONFIRMADA")) continue;
            LocalDate data = sessao.getDataHorario().toLocalDate();
            String cliente = sessao.getProjeto().getCliente().getNome();
            if (data.equals(hoje.plusDays(7))) {
                result.add(new DashboardResponse.Alerta("PRE_TATUAGEM", sessao.getId(), sessao.getProjeto().getId(), cliente,
                        data.toString(), "Confirmar os preparativos da tatuagem em 7 dias."));
            } else if (!data.isBefore(hoje.minusDays(30)) && !data.isAfter(hoje.minusDays(15))) {
                result.add(new DashboardResponse.Alerta("POS_TATUAGEM", sessao.getId(), sessao.getProjeto().getId(), cliente,
                        data.toString(), "Realizar o acompanhamento pós-tatuagem."));
            }
        }
        return result;
    }

    private boolean pendente(FollowUp followUp) {
        String status = followUp.getStatusContato();
        return status == null || (!status.equalsIgnoreCase("REALIZADO")
                && !status.equalsIgnoreCase("CONCLUIDO")
                && !status.equalsIgnoreCase("CANCELADO"));
    }
}
