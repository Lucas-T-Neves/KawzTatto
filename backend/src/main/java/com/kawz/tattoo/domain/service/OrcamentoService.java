package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.OrcamentoInput;
import com.kawz.tattoo.api.dto.ApiModels.OrcamentoResponse;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Cliente;
import com.kawz.tattoo.domain.model.Orcamento;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.repository.ClienteRepository;
import com.kawz.tattoo.domain.repository.OrcamentoRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class OrcamentoService {
    private final OrcamentoRepository orcamentos;
    private final ProjetoRepository projetos;
    private final ClienteRepository clientes;

    public OrcamentoService(OrcamentoRepository orcamentos, ProjetoRepository projetos, ClienteRepository clientes) {
        this.orcamentos = orcamentos;
        this.projetos = projetos;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public Page<OrcamentoResponse> listar(String status, Integer projetoId, Integer clienteId, Pageable pageable) {
        return orcamentos.buscar(normalizar(status), projetoId, clienteId, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscar(Integer id) {
        return toResponse(obter(id));
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarPorProjeto(Integer projetoId) {
        return orcamentos.findByProjetoIdOrderByDataOrcamentoDescIdDesc(projetoId)
                .stream().map(com.kawz.tattoo.api.dto.ApiMapper::toResponse).toList();
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoInput input) {
        validar(input);
        Projeto projeto = obterProjeto(input.projetoId());
        Cliente cliente = obterCliente(input.clienteId());
        validarVinculo(projeto, cliente);
        Orcamento orcamento = new Orcamento(projeto, cliente, nome(input), input.data(), input.status(),
                input.total(), input.deposito(), input.sessoesEstimadas(), input.formaPagamento(),
                input.validade(), input.propostaAceita());
        return toResponse(orcamentos.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse atualizar(Integer id, OrcamentoInput input) {
        validar(input);
        Orcamento orcamento = obter(id);
        Projeto projeto = obterProjeto(input.projetoId());
        Cliente cliente = obterCliente(input.clienteId());
        validarVinculo(projeto, cliente);
        orcamento.vincularProjeto(projeto);
        orcamento.vincularCliente(cliente);
        orcamento.atualizar(nome(input), input.data(), input.status(), input.total(), input.deposito(),
                input.sessoesEstimadas(), input.formaPagamento(), input.validade(), input.propostaAceita());
        return toResponse(orcamento);
    }

    @Transactional
    public void excluir(Integer id) {
        orcamentos.delete(obter(id));
    }

    private void validar(OrcamentoInput input) {
        if (input.deposito().compareTo(input.total()) > 0) {
            throw new BusinessException("O sinal não pode ser maior que o valor total");
        }
    }

    private void validarVinculo(Projeto projeto, Cliente cliente) {
        if (!projeto.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("O projeto selecionado não pertence ao cliente informado");
        }
    }

    private String nome(OrcamentoInput input) {
        if (input.nome() == null || input.nome().isBlank()) {
            return "Orçamento";
        }
        return input.nome().trim();
    }

    private Orcamento obter(Integer id) {
        return orcamentos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orcamento", id));
    }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Cliente obterCliente(Integer id) {
        return clientes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
