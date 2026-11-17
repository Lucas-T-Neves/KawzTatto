package com.kawz.tattoo.api.dashboard.dto;

import java.math.BigDecimal;

public record DashboardResponse(String mes, long projetosAtivos, long sessoesHoje,
                                    long followUpsPendentes, BigDecimal receitaMes,
                                    BigDecimal despesasMes, BigDecimal saldoMes,
                                    BigDecimal pendenciasMes) {}
