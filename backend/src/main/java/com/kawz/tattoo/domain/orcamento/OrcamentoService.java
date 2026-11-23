package com.kawz.tattoo.domain.orcamento;

import com.kawz.tattoo.api.orcamento.OrcamentoMapper;
import com.kawz.tattoo.api.orcamento.dto.OrcamentoInput;
import com.kawz.tattoo.api.orcamento.dto.OrcamentoResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orçamentos do tatuador autenticado. O proprietário é o dono do projeto; o
 * cliente informado também precisa pertencer ao mesmo tatuador.
 */
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
    public Page<OrcamentoResponse> listar(Integer tatuadorId, String status, Integer projetoId,
                                          Integer clienteId, Pageable pageable) {
        return orcamentos.buscar(tatuadorId, normalizar(status), projetoId, clienteId, pageable)
                .map(OrcamentoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscar(Integer id, Integer tatuadorId) {
        return OrcamentoMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarPorProjeto(Integer projetoId, Integer tatuadorId) {
        obterProjetoDoTatuador(projetoId, tatuadorId);
        return orcamentos.findByProjetoIdAndProjeto_Tatuador_IdOrderByDataOrcamentoDescIdDesc(projetoId, tatuadorId)
                .stream().map(OrcamentoMapper::toResponse).toList();
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoInput input, Integer tatuadorId) {
        validar(input);
        Projeto projeto = obterProjetoDoTatuador(input.projetoId(), tatuadorId);
        Cliente cliente = obterClienteDoTatuador(input.clienteId(), tatuadorId);
        validarVinculo(projeto, cliente);
        Orcamento orcamento = new Orcamento(projeto, cliente, nome(input), input.data(), input.status(),
                input.total(), input.deposito(), input.sessoesEstimadas(), input.formaPagamento(),
                input.validade(), input.propostaAceita());
        return OrcamentoMapper.toResponse(orcamentos.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse atualizar(Integer id, OrcamentoInput input, Integer tatuadorId) {
        validar(input);
        Orcamento orcamento = obterDoTatuador(id, tatuadorId);
        Projeto projeto = obterProjetoDoTatuador(input.projetoId(), tatuadorId);
        Cliente cliente = obterClienteDoTatuador(input.clienteId(), tatuadorId);
        validarVinculo(projeto, cliente);
        orcamento.vincularProjeto(projeto);
        orcamento.vincularCliente(cliente);
        orcamento.atualizar(nome(input), input.data(), input.status(), input.total(), input.deposito(),
                input.sessoesEstimadas(), input.formaPagamento(), input.validade(), input.propostaAceita());
        return OrcamentoMapper.toResponse(orcamento);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        orcamentos.delete(obterDoTatuador(id, tatuadorId));
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

    private Orcamento obterDoTatuador(Integer id, Integer tatuadorId) {
        return orcamentos.findByIdAndProjeto_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento", id));
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Cliente obterClienteDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
