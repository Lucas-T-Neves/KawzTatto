package com.kawz.tattoo.application.projeto;

import com.kawz.tattoo.domain.followup.FollowUp;
import com.kawz.tattoo.domain.imagem.Imagem;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoFinanceira;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.sessao.Sessao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado do caso de uso "projeto detalhado". É composto por objetos de domínio,
 * não por DTOs HTTP: a camada de API converte este resultado em
 * {@code ProjetoDetalhadoResponse} (ver {@code api.projeto.ProjetoDetalhadoMapper}).
 *
 * <p>O projeto vem com as características carregadas na mesma consulta,
 * então a conversão pode acontecer fora da transação.</p>
 */
public record ProjetoDetalhadoResult(
        Projeto projeto,
        BigDecimal recebido,
        boolean sinalPago,
        Imagem referencia,
        List<Sessao> sessoes,
        List<MovimentacaoFinanceira> movimentacoes,
        List<FollowUp> followUps,
        List<Imagem> imagens) {
}
