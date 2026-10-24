package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.ProjetoInput;
import com.kawz.tattoo.api.dto.ApiModels.ProjetoResponse;
import com.kawz.tattoo.api.dto.ApiModels.StatusRequest;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Cliente;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.model.Tatuador;
import com.kawz.tattoo.domain.repository.ClienteRepository;
import com.kawz.tattoo.domain.repository.MovimentacaoFinanceiraRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import com.kawz.tattoo.domain.repository.TatuadorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class ProjetoService {
    private static final Resumo SEM_MOVIMENTACOES = new Resumo(BigDecimal.ZERO, false);

    private final ProjetoRepository projetos;
    private final ClienteRepository clientes;
    private final TatuadorRepository tatuadores;
    private final MovimentacaoFinanceiraRepository movimentacoes;

    public ProjetoService(ProjetoRepository projetos, ClienteRepository clientes, TatuadorRepository tatuadores,
                          MovimentacaoFinanceiraRepository movimentacoes) {
        this.projetos = projetos;
        this.clientes = clientes;
        this.tatuadores = tatuadores;
        this.movimentacoes = movimentacoes;
    }

    private record Resumo(BigDecimal recebido, boolean sinalPago) {}

    @Transactional(readOnly = true)
    public Page<ProjetoResponse> listar(Pageable pageable) {
        Map<Integer, Resumo> resumos = new HashMap<>();
        for (Object[] linha : movimentacoes.resumirPorProjeto()) {
            resumos.put((Integer) linha[0], new Resumo((BigDecimal) linha[1],
                    ((Number) linha[2]).intValue() == 1));
        }
        return projetos.findAll(pageable).map(projeto -> {
            Resumo resumo = resumos.getOrDefault(projeto.getId(), SEM_MOVIMENTACOES);
            return toResponse(projeto, resumo.recebido(), resumo.sinalPago());
        });
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscar(Integer id) {
        Resumo resumo = resumo(id);
        return toResponse(obter(id), resumo.recebido(), resumo.sinalPago());
    }

    @Transactional
    public ProjetoResponse criar(ProjetoInput input) {
        validarValores(input);
        Projeto projeto = new Projeto(obterTatuador(input.tatuadorId()), obterCliente(input.clienteId()),
                input.descricao(), input.statusProjeto(), input.valorTotal(), input.valorSinal(),
                input.quantidadeSessoes(), input.dataValidade());
        // Projeto novo: nenhuma movimentação possível ainda
        return toResponse(projetos.save(projeto), BigDecimal.ZERO, false);
    }

    @Transactional
    public ProjetoResponse atualizar(Integer id, ProjetoInput input) {
        validarValores(input);
        Projeto projeto = obter(id);
        projeto.vincular(obterTatuador(input.tatuadorId()), obterCliente(input.clienteId()));
        projeto.atualizar(input.descricao(), input.statusProjeto(), input.valorTotal(), input.valorSinal(),
                input.quantidadeSessoes(), input.dataValidade());
        Resumo resumo = resumo(id);
        return toResponse(projeto, resumo.recebido(), resumo.sinalPago());
    }

    @Transactional
    public ProjetoResponse alterarStatus(Integer id, StatusRequest input) {
        Projeto projeto = obter(id);
        projeto.alterarStatus(input.status());
        Resumo resumo = resumo(id);
        return toResponse(projeto, resumo.recebido(), resumo.sinalPago());
    }

    @Transactional
    public void excluir(Integer id) { projetos.delete(obter(id)); }

    private Resumo resumo(Integer projetoId) {
        BigDecimal recebido = movimentacoes.somarRecebido(projetoId);
        boolean sinalPago = movimentacoes.existsByProjetoIdAndCategoriaIgnoreCaseAndStatusPagamentoIgnoreCase(
                projetoId, "sinal", "pago");
        return new Resumo(recebido == null ? BigDecimal.ZERO : recebido, sinalPago);
    }

    private void validarValores(ProjetoInput input) {
        if (input.valorSinal().compareTo(input.valorTotal()) > 0) {
            throw new BusinessException("O sinal não pode ser maior que o valor total");
        }
    }

    private Projeto obter(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Cliente obterCliente(Integer id) {
        return clientes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Tatuador obterTatuador(Integer id) {
        return tatuadores.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tatuador", id));
    }
}
