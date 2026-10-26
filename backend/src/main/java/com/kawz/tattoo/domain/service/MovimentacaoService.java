package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoInput;
import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoResponse;
import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoResumoResponse;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.MovimentacaoFinanceira;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.repository.MovimentacaoFinanceiraRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class MovimentacaoService {
    private final MovimentacaoFinanceiraRepository movimentacoes;
    private final ProjetoRepository projetos;

    public MovimentacaoService(MovimentacaoFinanceiraRepository movimentacoes, ProjetoRepository projetos) {
        this.movimentacoes = movimentacoes;
        this.projetos = projetos;
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listar(String mes, String status, Integer projetoId,
                                              Integer clienteId, Pageable pageable) {
        LocalDate inicio = null;
        LocalDate fim = null;
        if (mes != null && !mes.isBlank()) {
            try {
                YearMonth periodo = YearMonth.parse(mes);
                inicio = periodo.atDay(1);
                fim = periodo.plusMonths(1).atDay(1);
            } catch (RuntimeException ex) {
                throw new BusinessException("O parâmetro mes deve estar no formato YYYY-MM");
            }
        }
        return movimentacoes.buscar(inicio, fim, normalizar(status), projetoId, clienteId, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listarPorProjeto(Integer projetoId, Pageable pageable) {
        return movimentacoes.findByProjetoIdOrderByDataOcorrenciaDesc(projetoId, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResponse buscarPorProjeto(Integer projetoId, Integer id) {
        MovimentacaoFinanceira movimentacao = obter(id);
        if (!movimentacao.getProjeto().getId().equals(projetoId)) {
            throw new ResourceNotFoundException("MovimentacaoFinanceira", id);
        }
        return toResponse(movimentacao);
    }

    @Transactional(readOnly = true)
    public MovimentacaoResumoResponse resumo(String mes) {
        if (mes == null || mes.isBlank()) {
            throw new BusinessException("O parâmetro mes é obrigatório no formato YYYY-MM");
        }
        final YearMonth periodo;
        try {
            periodo = YearMonth.parse(mes);
        } catch (RuntimeException ex) {
            throw new BusinessException("O parâmetro mes deve estar no formato YYYY-MM");
        }
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.plusMonths(1).atDay(1);
        List<MovimentacaoFinanceira> doMes =
                movimentacoes.findByDataOcorrenciaGreaterThanEqualAndDataOcorrenciaLessThan(inicio, fim);
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
    public MovimentacaoResponse criar(MovimentacaoInput input) {
        Projeto projeto = obterProjeto(input.projetoId());
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(projeto,
                input.naturezaTransacao(), input.categoria(), input.dataOcorrencia(),
                input.valorMonetario(), input.formaPagamento(), input.statusPagamento());
        movimentacao.registrar();
        atualizarStatusClientePorSinal(movimentacao);
        return toResponse(movimentacoes.save(movimentacao));
    }

    @Transactional
    public MovimentacaoResponse atualizar(Integer id, MovimentacaoInput input) {
        MovimentacaoFinanceira movimentacao = obter(id);
        movimentacao.vincularProjeto(obterProjeto(input.projetoId()));
        movimentacao.atualizar(input.naturezaTransacao(), input.categoria(), input.dataOcorrencia(),
                input.valorMonetario(), input.formaPagamento(), input.statusPagamento());
        atualizarStatusClientePorSinal(movimentacao);
        return toResponse(movimentacao);
    }

    @Transactional
    public void excluir(Integer id) { movimentacoes.delete(obter(id)); }

    private MovimentacaoFinanceira obter(Integer id) {
        return movimentacoes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MovimentacaoFinanceira", id));
    }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
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
