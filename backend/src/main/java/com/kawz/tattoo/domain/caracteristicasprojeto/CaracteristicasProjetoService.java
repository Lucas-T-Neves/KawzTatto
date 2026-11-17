package com.kawz.tattoo.domain.caracteristicasprojeto;

import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoInput;
import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.caracteristicasprojeto.CaracteristicasProjeto;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kawz.tattoo.api.caracteristicasprojeto.mapper.CaracteristicasprojetoApiMapper.toResponse;

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
