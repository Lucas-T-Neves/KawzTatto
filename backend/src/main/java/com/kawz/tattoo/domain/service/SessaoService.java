package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.SessaoInput;
import com.kawz.tattoo.api.dto.ApiModels.SessaoResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.model.Sessao;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import com.kawz.tattoo.domain.repository.SessaoRepository;
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

    public SessaoService(SessaoRepository sessoes, ProjetoRepository projetos) {
        this.sessoes = sessoes;
        this.projetos = projetos;
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
    public Page<SessaoResponse> listarPorDataApos(LocalDateTime dataHorario, Pageable pageable) {
        return sessoes.findByDataHorarioAfterOrderByDataHorarioAsc(dataHorario, pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SessaoResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional
    public SessaoResponse criar(SessaoInput input) {
        Projeto projeto = obterProjeto(input.projetoId());
        Sessao sessao = new Sessao(projeto, input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        return toResponse(sessoes.save(sessao));
    }

    @Transactional
    public SessaoResponse atualizar(Integer id, SessaoInput input) {
        Sessao sessao = obter(id);
        sessao.vincularProjeto(obterProjeto(input.projetoId()));
        sessao.atualizar(input.dataHorario(), input.localAtendimento(),
                input.statusSessao(), input.observacoesSessao());
        return toResponse(sessao);
    }

    @Transactional
    public SessaoResponse remarcar(Integer id, LocalDateTime novoHorario) {
        Sessao sessao = obter(id);
        sessao.remarcar(novoHorario);
        return toResponse(sessao);
    }

    @Transactional
    public void excluir(Integer id) { sessoes.delete(obter(id)); }

    private Sessao obter(Integer id) {
        return sessoes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sessao", id));
    }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
