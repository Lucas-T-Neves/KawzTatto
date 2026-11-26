package com.kawz.tattoo.domain.sessao;

import com.kawz.tattoo.api.sessao.SessaoMapper;
import com.kawz.tattoo.api.sessao.dto.SessaoInput;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.application.gateway.AgendaGateway;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.exception.SessaoInvalidaException;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Sessões do tatuador autenticado. Toda consulta é filtrada pelo dono do projeto
 * e o projeto informado na criação/atualização precisa pertencer ao tatuador.
 *
 * <p>A agenda externa é acessada por {@link AgendaGateway}. Se criar/atualizar o
 * evento falhar, a transação é desfeita (502) para o banco não divergir da agenda.</p>
 */
@Service
public class SessaoService {

    private final SessaoRepository sessoes;
    private final ProjetoRepository projetos;
    private final ClienteRepository clientes;
    private final AgendaGateway agenda;
    private final AgendaSyncPendenteRepository pendencias;

    public SessaoService(SessaoRepository sessoes, ProjetoRepository projetos,
                         ClienteRepository clientes, AgendaGateway agenda, AgendaSyncPendenteRepository pendencias) {
        this.sessoes = sessoes;
        this.projetos = projetos;
        this.clientes = clientes;
        this.agenda = agenda;
        this.pendencias = pendencias;
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listar(Integer tatuadorId, Pageable pageable) {
       return sessoes.findByProjeto_Tatuador_Id(tatuadorId, pageable).map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorProjeto(Integer projetoId, Integer tatuadorId, Pageable pageable) {
        obterProjetoDoTatuador(projetoId, tatuadorId);
         return sessoes.findByProjetoIdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(projetoId, tatuadorId, pageable)
                .map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorCliente(Integer clienteId, Integer tatuadorId, Pageable pageable) {
        if (!clientes.existsByIdAndTatuadorId(clienteId, tatuadorId)) {
            throw new ResourceNotFoundException("Cliente", clienteId);
        }
        return sessoes.findByProjeto_Cliente_IdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
                        clienteId, tatuadorId, pageable)
                .map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorDataApos(LocalDateTime dataHorario, Integer tatuadorId, Pageable pageable) {
         return sessoes.findByDataHorarioAfterAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
                        dataHorario, tatuadorId, pageable)
                .map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim,
                                                 Integer tatuadorId, Pageable pageable) {
        return sessoes.buscarPorPeriodo(inicio, fim, tatuadorId, pageable).map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SessaoResponse buscar(Integer id, Integer tatuadorId) {
        return SessaoMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public SessaoResponse criar(SessaoInput input, Integer tatuadorId) {
        validarDuracao(input.duracaoHoras());
        Projeto projeto = obterProjetoDoTatuador(input.projetoId(), tatuadorId);
        Sessao sessao = new Sessao(projeto, input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao(), input.duracaoHoras());
        Sessao salva = sessoes.save(sessao);
        sincronizarOuEnfileirar(salva);
        return SessaoMapper.toResponse(salva);
    }

    @Transactional
    public SessaoResponse atualizar(Integer id, SessaoInput input, Integer tatuadorId) {
        validarDuracao(input.duracaoHoras());
        Sessao sessao = obterDoTatuador(id, tatuadorId);
        sessao.vincularProjeto(obterProjetoDoTatuador(input.projetoId(), tatuadorId));
        sessao.atualizar(input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        sessao.definirDuracaoHoras(input.duracaoHoras());
        sincronizarOuEnfileirar(sessao);
        return SessaoMapper.toResponse(sessao);
    }

    @Transactional
    public SessaoResponse remarcar(Integer id, LocalDateTime novoHorario, Integer tatuadorId) {
        Sessao sessao = obterDoTatuador(id, tatuadorId);
        sessao.remarcar(novoHorario);
        sincronizarOuEnfileirar(sessao);
        return SessaoMapper.toResponse(sessao);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        Sessao sessao = obterDoTatuador(id, tatuadorId);
        String eventoId = sessao.getEventoCalendarioId();
        pendencias.deleteBySessaoId(sessao.getId());
        sessoes.delete(sessao);
        sessoes.flush();
        agenda.removerEvento(eventoId);
    }

    /** Cria ou atualiza o evento da sessão no calendário. Não faz nada se a integração estiver desligada. */
    public void sincronizarAgenda(Sessao sessao) {
        if (!agenda.ativo()) return;
        if ("CANCELADA".equalsIgnoreCase(sessao.getStatusSessao())) {
            if (sessao.getEventoCalendarioId() != null) {
                agenda.removerEvento(sessao.getEventoCalendarioId());
                sessao.registrarEventoCalendario(null);
            }
            return;
        }
        Projeto projeto = sessao.getProjeto();
        String titulo = "Sessão - " + projeto.getCliente().getNome();
        String descricao = projeto.getDescricao();
        if (sessao.getEventoCalendarioId() == null) {
            sessao.registrarEventoCalendario(agenda.criarEvento(
                    titulo, descricao, sessao.getLocalAtendimento(), sessao.getDataHorario(), sessao.getDuracaoHoras()));
        } else {
            agenda.atualizarEvento(sessao.getEventoCalendarioId(),
                    titulo, descricao, sessao.getLocalAtendimento(), sessao.getDataHorario(), sessao.getDuracaoHoras());
        }
    }

    private void sincronizarOuEnfileirar(Sessao sessao) {
        try {
            sincronizarAgenda(sessao);
        } catch (RuntimeException ex) {
            if (!pendencias.existsBySessaoId(sessao.getId())) {
                pendencias.save(new AgendaSyncPendente(sessao, "SINCRONIZAR"));
            }
        }
    }

    private Sessao obterDoTatuador(Integer id, Integer tatuadorId) {
        return sessoes.findByIdAndProjeto_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessao", id));
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private void validarDuracao(Integer duracaoHoras) {
        if (duracaoHoras != null && (duracaoHoras < 1 || duracaoHoras > 24)) {
            throw new SessaoInvalidaException("A duração da sessão deve estar entre 1 e 24 horas.");
        }
    }
}
