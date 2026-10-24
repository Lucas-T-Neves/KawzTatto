package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.AtividadeInput;
import com.kawz.tattoo.api.dto.ApiModels.AtividadeResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Atividade;
import com.kawz.tattoo.domain.model.Tatuador;
import com.kawz.tattoo.domain.repository.AtividadeRepository;
import com.kawz.tattoo.domain.repository.TatuadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class AtividadeService {
    private final AtividadeRepository atividades;
    private final TatuadorRepository tatuadores;

    public AtividadeService(AtividadeRepository atividades, TatuadorRepository tatuadores) {
        this.atividades = atividades;
        this.tatuadores = tatuadores;
    }

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listarPorTatuador(Integer tatuadorId) {
        return atividades.findByTatuadorIdOrderByIdDesc(tatuadorId).stream()
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AtividadeResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional
    public AtividadeResponse criar(AtividadeInput input) {
        Tatuador tatuador = tatuadores.findById(input.tatuadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador", input.tatuadorId()));
        return toResponse(atividades.save(new Atividade(tatuador, input.descricao(), input.tipo())));
    }

    @Transactional
    public AtividadeResponse atualizar(Integer id, AtividadeInput input) {
        Atividade atividade = obter(id);
        Tatuador tatuador = tatuadores.findById(input.tatuadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador", input.tatuadorId()));
        atividade.vincularTatuador(tatuador);
        atividade.atualizar(input.descricao(), input.tipo());
        return toResponse(atividade);
    }

    @Transactional
    public void excluir(Integer id) { atividades.delete(obter(id)); }

    private Atividade obter(Integer id) {
        return atividades.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade", id));
    }
}
