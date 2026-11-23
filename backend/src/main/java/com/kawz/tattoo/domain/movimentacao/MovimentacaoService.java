package com.kawz.tattoo.domain.movimentacao;

import com.kawz.tattoo.api.movimentacao.MovimentacaoMapper;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoInput;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResumoResponse;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Movimentações financeiras dos projetos do tatuador autenticado. O financeiro é
 * sempre recortado pelo dono do projeto: um tatuador não vê nem altera valores
 * de outro.
 */
@Service
public class MovimentacaoService {

    private final MovimentacaoFinanceiraRepository movimentacoes;
    private final ProjetoRepository projetos;

    public MovimentacaoService(MovimentacaoFinanceiraRepository movimentacoes, ProjetoRepository projetos) {
        this.movimentacoes = movimentacoes;
        this.projetos = projetos;
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listar(Integer tatuadorId, String mes, String status, Integer projetoId,
                                             Integer clienteId, Pageable pageable) {
        LocalDate inicio = null;
        LocalDate fim = null;
        if (mes != null && !mes.isBlank()) {
            YearMonth periodo = periodo(mes);
            inicio = periodo.atDay(1);
            fim = periodo.plusMonths(1).atDay(1);
        }
        return movimentacoes.buscar(tatuadorId, inicio, fim, normalizar(status), projetoId, clienteId, pageable)
                .map(MovimentacaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResponse buscar(Integer id, Integer tatuadorId) {
        return MovimentacaoMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listarPorProjeto(Integer projetoId, Integer tatuadorId, Pageable pageable) {
        obterProjetoDoTatuador(projetoId, tatuadorId);
        // Page.map converte a página de movimentações do projeto sem
        // reconstruí-la manualmente com PageImpl.
        return movimentacoes.findByProjetoIdAndProjeto_Tatuador_IdOrderByDataOcorrenciaDesc(
                        projetoId, tatuadorId, pageable)
                .map(MovimentacaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResponse buscarPorProjeto(Integer projetoId, Integer id, Integer tatuadorId) {
        MovimentacaoFinanceira movimentacao = obterDoTatuador(id, tatuadorId);
        if (!movimentacao.getProjeto().getId().equals(projetoId)) {
            throw new ResourceNotFoundException("MovimentacaoFinanceira", id);
        }
        return MovimentacaoMapper.toResponse(movimentacao);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResumoResponse resumo(String mes, Integer tatuadorId) {
        if (mes == null || mes.isBlank()) {
            throw new BusinessException("O parâmetro mes é obrigatório no formato YYYY-MM");
        }
        YearMonth periodo = periodo(mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.plusMonths(1).atDay(1);
        List<MovimentacaoFinanceira> doMes =
                movimentacoes.findByProjeto_Tatuador_IdAndDataOcorrenciaGreaterThanEqualAndDataOcorrenciaLessThan(
                        tatuadorId, inicio, fim);
        BigDecimal receita = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;
        BigDecimal receitasPendentes = BigDecimal.ZERO;
        BigDecimal despesasPendentes = BigDecimal.ZERO;
        for (MovimentacaoFinanceira movimentacao : doMes) {
            BigDecimal valor = movimentacao.getValorMonetario() == null
                    ? BigDecimal.ZERO : movimentacao.getValorMonetario();
            boolean paga = "pago".equalsIgnoreCase(movimentacao.getStatusPagamento());
            boolean entrada = "ENTRADA".equalsIgnoreCase(movimentacao.getNaturezaTransacao());
            if (entrada && paga) receita = receita.add(valor);
            else if (!entrada && paga) despesas = despesas.add(valor);
            else if (entrada) receitasPendentes = receitasPendentes.add(valor);
            else despesasPendentes = despesasPendentes.add(valor);
        }
        BigDecimal pendencias = receitasPendentes.add(despesasPendentes);
        return new MovimentacaoResumoResponse(periodo.toString(), receita, despesas,
                receita.subtract(despesas), pendencias, receitasPendentes, despesasPendentes);
    }

    @Transactional
    public MovimentacaoResponse criar(MovimentacaoInput input, Integer tatuadorId) {
        Projeto projeto = obterProjetoDoTatuador(input.projetoId(), tatuadorId);
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(projeto,
                input.naturezaTransacao(), input.categoria(), input.dataOcorrencia(),
                input.valorMonetario(), input.formaPagamento(), input.statusPagamento());
        movimentacao.registrar();
        atualizarStatusClientePorSinal(movimentacao);
        return MovimentacaoMapper.toResponse(movimentacoes.save(movimentacao));
    }

    @Transactional
    public MovimentacaoResponse atualizar(Integer id, MovimentacaoInput input, Integer tatuadorId) {
        MovimentacaoFinanceira movimentacao = obterDoTatuador(id, tatuadorId);
        movimentacao.vincularProjeto(obterProjetoDoTatuador(input.projetoId(), tatuadorId));
        movimentacao.atualizar(input.naturezaTransacao(), input.categoria(), input.dataOcorrencia(),
                input.valorMonetario(), input.formaPagamento(), input.statusPagamento());
        atualizarStatusClientePorSinal(movimentacao);
        return MovimentacaoMapper.toResponse(movimentacao);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        movimentacoes.delete(obterDoTatuador(id, tatuadorId));
    }

    private MovimentacaoFinanceira obterDoTatuador(Integer id, Integer tatuadorId) {
        return movimentacoes.findByIdAndProjeto_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("MovimentacaoFinanceira", id));
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private YearMonth periodo(String mes) {
        try {
            return YearMonth.parse(mes);
        } catch (RuntimeException ex) {
            throw new BusinessException("O parâmetro mes deve estar no formato YYYY-MM");
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void atualizarStatusClientePorSinal(MovimentacaoFinanceira movimentacao) {
        boolean sinalPago = "ENTRADA".equalsIgnoreCase(movimentacao.getNaturezaTransacao())
                && "sinal".equalsIgnoreCase(movimentacao.getCategoria())
                && "pago".equalsIgnoreCase(movimentacao.getStatusPagamento());
        if (sinalPago) {
            movimentacao.getProjeto().getCliente().converterEmCliente();
        }
    }
}
