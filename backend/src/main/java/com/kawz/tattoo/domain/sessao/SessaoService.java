package com.kawz.tattoo.domain.sessao;

import com.kawz.tattoo.api.sessao.SessaoMapper;
import com.kawz.tattoo.api.sessao.dto.SessaoInput;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.application.gateway.AgendaGateway;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
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

    public SessaoService(SessaoRepository sessoes, ProjetoRepository projetos,
                         ClienteRepository clientes, AgendaGateway agenda) {
        this.sessoes = sessoes;
        this.projetos = projetos;
        this.clientes = clientes;
        this.agenda = agenda;
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listar(Integer tatuadorId, Pageable pageable) {
        // Page.map simplifica a conversão de cada Sessao para SessaoResponse
        // sem perder os metadados de paginação devolvidos pelo repository.
        return sessoes.findByProjeto_Tatuador_Id(tatuadorId, pageable).map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorProjeto(Integer projetoId, Integer tatuadorId, Pageable pageable) {
        obterProjetoDoTatuador(projetoId, tatuadorId);
        // Page.map substitui cada entidade pelo DTO correspondente e mantém
        // as informações da página original, como totalElements e totalPages.
        return sessoes.findByProjetoIdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(projetoId, tatuadorId, pageable)
                .map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorCliente(Integer clienteId, Integer tatuadorId, Pageable pageable) {
        if (!clientes.existsByIdAndTatuadorId(clienteId, tatuadorId)) {
            throw new ResourceNotFoundException("Cliente", clienteId);
        }
        // Page.map concentra a conversão item a item da página sem alterar
        // o contrato paginado entregue ao controller e ao frontend.
        return sessoes.findByProjeto_Cliente_IdAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
                        clienteId, tatuadorId, pageable)
                .map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorDataApos(LocalDateTime dataHorario, Integer tatuadorId, Pageable pageable) {
        // Page.map evita uma conversão manual com PageImpl e preserva os
        // metadados de paginação enquanto aplica o SessaoMapper.
        return sessoes.findByDataHorarioAfterAndProjeto_Tatuador_IdOrderByDataHorarioAsc(
                        dataHorario, tatuadorId, pageable)
                .map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponse> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim,
                                                 Integer tatuadorId, Pageable pageable) {
        // Page.map realiza a transformação entidade -> DTO e conserva
        // a página original, inclusive a contagem total de registros.
        return sessoes.buscarPorPeriodo(inicio, fim, tatuadorId, pageable).map(SessaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SessaoResponse buscar(Integer id, Integer tatuadorId) {
        return SessaoMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public SessaoResponse criar(SessaoInput input, Integer tatuadorId) {
        Projeto projeto = obterProjetoDoTatuador(input.projetoId(), tatuadorId);
        Sessao sessao = new Sessao(projeto, input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        Sessao salva = sessoes.save(sessao);
        sincronizarAgenda(salva); // se a agenda falhar, a exceção desfaz a sessão (transação)
        return SessaoMapper.toResponse(salva);
    }

    @Transactional
    public SessaoResponse atualizar(Integer id, SessaoInput input, Integer tatuadorId) {
        Sessao sessao = obterDoTatuador(id, tatuadorId);
        sessao.vincularProjeto(obterProjetoDoTatuador(input.projetoId(), tatuadorId));
        sessao.atualizar(input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        sincronizarAgenda(sessao);
        return SessaoMapper.toResponse(sessao);
    }

    @Transactional
    public SessaoResponse remarcar(Integer id, LocalDateTime novoHorario, Integer tatuadorId) {
        Sessao sessao = obterDoTatuador(id, tatuadorId);
        sessao.remarcar(novoHorario);
        sincronizarAgenda(sessao);
        return SessaoMapper.toResponse(sessao);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        Sessao sessao = obterDoTatuador(id, tatuadorId);
        String eventoId = sessao.getEventoCalendarioId();
        sessoes.delete(sessao);
        sessoes.flush();
        agenda.removerEvento(eventoId);
    }

    /** Cria ou atualiza o evento da sessão no calendário. Não faz nada se a integração estiver desligada. */
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

    private Sessao obterDoTatuador(Integer id, Integer tatuadorId) {
        return sessoes.findByIdAndProjeto_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessao", id));
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
