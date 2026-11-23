package com.kawz.tattoo.application.projeto;

import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.followup.FollowUpRepository;
import com.kawz.tattoo.domain.imagem.Imagem;
import com.kawz.tattoo.domain.imagem.ImagemRepository;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoFinanceiraRepository;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import com.kawz.tattoo.domain.sessao.SessaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Caso de uso de leitura que compõe a tela de projeto detalhado.
 *
 * <p>Antes essa composição ficava no controller, que injetava sete services.
 * Agora o controller é só um adaptador HTTP: recebe o id, chama este caso de uso
 * e converte o resultado em resposta.</p>
 */
@Service
public class BuscarProjetoDetalhadoUseCase {

    private final ProjetoRepository projetos;
    private final SessaoRepository sessoes;
    private final MovimentacaoFinanceiraRepository movimentacoes;
    private final FollowUpRepository followUps;
    private final ImagemRepository imagens;

    public BuscarProjetoDetalhadoUseCase(ProjetoRepository projetos, SessaoRepository sessoes,
                                         MovimentacaoFinanceiraRepository movimentacoes,
                                         FollowUpRepository followUps, ImagemRepository imagens) {
        this.projetos = projetos;
        this.sessoes = sessoes;
        this.movimentacoes = movimentacoes;
        this.followUps = followUps;
        this.imagens = imagens;
    }

    @Transactional(readOnly = true)
    public ProjetoDetalhadoResult executar(Integer projetoId, Integer tatuadorId) {
        Projeto projeto = projetos.buscarComDetalhesDoTatuador(projetoId, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", projetoId));

        BigDecimal recebido = movimentacoes.somarRecebido(projetoId);
        boolean sinalPago = movimentacoes
                .existsByProjetoIdAndCategoriaIgnoreCaseAndStatusPagamentoIgnoreCase(projetoId, "sinal", "pago");
        Imagem referencia = imagens
                .findFirstByProjetoIdAndProjeto_Tatuador_IdOrderByCriadoEmDesc(projetoId, tatuadorId)
                .orElse(null);

        return new ProjetoDetalhadoResult(
                projeto,
                recebido == null ? BigDecimal.ZERO : recebido,
                sinalPago,
                referencia,
                sessoes.findByProjetoIdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(projetoId, tatuadorId),
                movimentacoes.findByProjetoIdAndProjeto_Tatuador_IdOrderByDataOcorrenciaDescIdDesc(projetoId, tatuadorId),
                followUps.findByProjetoIdDoTatuador(projetoId, tatuadorId),
                imagens.findByProjetoIdAndProjeto_Tatuador_IdOrderByCriadoEmDesc(projetoId, tatuadorId));
    }
}
