package com.kawz.tattoo.domain.caracteristicasprojeto;

import com.kawz.tattoo.api.caracteristicasprojeto.CaracteristicasProjetoMapper;
import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoInput;
import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Características do projeto: o acesso é autorizado pela posse do projeto. */
@Service
public class CaracteristicasProjetoService {

    private final ProjetoRepository projetos;

    public CaracteristicasProjetoService(ProjetoRepository projetos) { this.projetos = projetos; }

    @Transactional(readOnly = true)
    public CaracteristicasProjetoResponse buscar(Integer projetoId, Integer tatuadorId) {
        Projeto projeto = obterProjetoDoTatuador(projetoId, tatuadorId);
        return CaracteristicasProjetoMapper.toResponse(projeto.getCaracteristicas(), projetoId);
    }

    @Transactional
    public CaracteristicasProjetoResponse atualizar(Integer projetoId, CaracteristicasProjetoInput input,
                                                    Integer tatuadorId) {
        Projeto projeto = obterProjetoDoTatuador(projetoId, tatuadorId);
        CaracteristicasProjeto caracteristicas = projeto.getCaracteristicas();
        caracteristicas.atualizarCaracteristicas(input.ideiaExpectativa(), input.estiloArte(),
                input.localizacaoCorpo(), input.tamanho(), input.dificuldade(), input.cores(), input.complexidade());
        return CaracteristicasProjetoMapper.toResponse(caracteristicas, projetoId);
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
