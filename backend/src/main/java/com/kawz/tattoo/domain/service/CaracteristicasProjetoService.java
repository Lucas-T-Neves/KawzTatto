package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.CaracteristicasProjetoInput;
import com.kawz.tattoo.api.dto.ApiModels.CaracteristicasProjetoResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.CaracteristicasProjeto;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class CaracteristicasProjetoService {
    private final ProjetoRepository projetos;

    public CaracteristicasProjetoService(ProjetoRepository projetos) { this.projetos = projetos; }

    @Transactional(readOnly = true)
    public CaracteristicasProjetoResponse buscar(Integer projetoId) {
        Projeto projeto = obterProjeto(projetoId);
        return toResponse(projeto.getCaracteristicas(), projetoId);
    }

    @Transactional
    public CaracteristicasProjetoResponse atualizar(Integer projetoId, CaracteristicasProjetoInput input) {
        Projeto projeto = obterProjeto(projetoId);
        CaracteristicasProjeto caracteristicas = projeto.getCaracteristicas();
        caracteristicas.atualizarCaracteristicas(input.ideiaExpectativa(), input.estiloArte(),
                input.localizacaoCorpo(), input.tamanho(), input.dificuldade(), input.cores(), input.complexidade());
        return toResponse(caracteristicas, projetoId);
    }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
