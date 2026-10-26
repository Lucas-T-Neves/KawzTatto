package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.SessaoInput;
import com.kawz.tattoo.api.dto.ApiModels.SessaoResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.model.Sessao;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import com.kawz.tattoo.domain.repository.SessaoRepository;
import com.kawz.tattoo.integration.NylasCalendarClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class SessaoService {
    private final SessaoRepository sessoes;
    private final ProjetoRepository projetos;
    private final NylasCalendarClient agenda;

    public SessaoService(SessaoRepository sessoes, ProjetoRepository projetos, NylasCalendarClient agenda) {
        this.sessoes = sessoes;
        this.projetos = projetos;
        this.agenda = agenda;
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listar(Pageable pageable) {
        return sessoes.findAll(pageable).map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorProjeto(Integer projetoId, Pageable pageable) {
        return sessoes.findByProjetoIdOrderByDataHorarioAsc(projetoId, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        return sessoes.findByProjetoClienteIdOrderByDataHorarioAsc(clienteId, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorDataApos(LocalDateTime dataHorario, Pageable pageable) {
        return sessoes.findByDataHorarioAfterOrderByDataHorarioAsc(dataHorario, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        return sessoes.buscarPorPeriodo(inicio, fim, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SessaoResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional
    public SessaoResponse criar(SessaoInput input) {
        Projeto projeto = obterProjeto(input.projetoId());
        Sessao sessao = new Sessao(projeto, input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        Sessao salva = sessoes.save(sessao);
        sincronizarAgenda(salva); // se o Nylas falhar, a exceção desfaz a sessão (transação)
        return toResponse(salva);
    }

    @Transactional
    public SessaoResponse atualizar(Integer id, SessaoInput input) {
        Sessao sessao = obter(id);
        sessao.vincularProjeto(obterProjeto(input.projetoId()));
        sessao.atualizar(input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        sincronizarAgenda(sessao);
        return toResponse(sessao);
    }

    @Transactional
    public SessaoResponse remarcar(Integer id, LocalDateTime novoHorario) {
        Sessao sessao = obter(id);
        sessao.remarcar(novoHorario);
        sincronizarAgenda(sessao);
        return toResponse(sessao);
    }

    @Transactional
    public void excluir(Integer id) {
        Sessao sessao = obter(id);
        String eventoId = sessao.getEventoCalendarioId();
        sessoes.delete(sessao);
        sessoes.flush();
        agenda.removerEvento(eventoId);
    }

    /** Cria ou atualiza o evento da sessão no calendário (Nylas). Não faz nada se a integração estiver desligada. */
    private void sincronizarAgenda(Sessao sessao) {
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
                    titulo, descricao, sessao.getLocalAtendimento(), sessao.getDataHorario()));
        } else {
            agenda.atualizarEvento(sessao.getEventoCalendarioId(),
                    titulo, descricao, sessao.getLocalAtendimento(), sessao.getDataHorario());
        }
    }

    private Sessao obter(Integer id) {
        return sessoes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sessao", id));
    }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
