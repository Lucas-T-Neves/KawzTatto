package com.kawz.tattoo.api.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(String mes, long projetosAtivos, long sessoesHoje,
                                    long followUpsPendentes, BigDecimal receitaMes,
                                    BigDecimal despesasMes, BigDecimal saldoMes,
                                    BigDecimal pendenciasMes, List<Alerta> alertas) {
    public record Alerta(String tipo, Integer sessaoId, Integer projetoId, String cliente,
                         String data, String mensagem) {}
}
