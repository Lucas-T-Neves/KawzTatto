package com.kawz.tattoo.domain.atividade;

import com.kawz.tattoo.api.atividade.AtividadeMapper;
import com.kawz.tattoo.api.atividade.dto.AtividadeInput;
import com.kawz.tattoo.api.atividade.dto.AtividadeResponse;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.tatuador.Tatuador;
import com.kawz.tattoo.domain.tatuador.TatuadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Atividades da agenda do tatuador autenticado.
 *
 * <p>As rotas recebem {@code tatuadorId} no caminho por compatibilidade com o
 * frontend, mas o valor precisa ser o do tatuador autenticado; caso contrário a
 * resposta é 404 (não confirmamos a existência de agendas alheias).</p>
 */
@Service
public class AtividadeService {

    private final AtividadeRepository atividades;
    private final TatuadorRepository tatuadores;

    public AtividadeService(AtividadeRepository atividades, TatuadorRepository tatuadores) {
        this.atividades = atividades;
        this.tatuadores = tatuadores;
    }

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listarPorTatuador(Integer tatuadorId, Integer tatuadorAutenticadoId) {
        validarTatuador(tatuadorId, tatuadorAutenticadoId);
        // stream().map().toList() percorre as atividades e aplica o mapper
        // explícito para produzir a lista de respostas da API.
        return atividades.findByTatuadorIdOrderByIdDesc(tatuadorId).stream()
                .map(AtividadeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listarPorTatuadorEData(Integer tatuadorId, LocalDate data,
                                                          Integer tatuadorAutenticadoId) {
        validarTatuador(tatuadorId, tatuadorAutenticadoId);
        // A abstração stream().map().toList() evita repetir manualmente
        // a conversão de cada atividade e devolve uma nova lista de DTOs.
        return atividades.findByTatuadorIdAndDataOrderByInicioAsc(tatuadorId, data).stream()
                .map(AtividadeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AtividadeResponse buscar(Integer id, Integer tatuadorId) {
        return AtividadeMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public AtividadeResponse criar(AtividadeInput input, Integer tatuadorId) {
        Tatuador tatuador = obterTatuador(tatuadorId);
        validarHorario(input);
        Atividade salva = atividades.save(new Atividade(tatuador, input.projetoId(), input.clienteId(),
                input.descricao(), input.tipo(), input.status(), input.titulo(), input.data(),
                input.inicio(), input.termino(), input.local(), input.observacoes()));
        return AtividadeMapper.toResponse(salva);
    }

    @Transactional
    public AtividadeResponse atualizar(Integer id, AtividadeInput input, Integer tatuadorId) {
        Atividade atividade = obterDoTatuador(id, tatuadorId);
        validarHorario(input);
        atividade.vincularTatuador(obterTatuador(tatuadorId));
        atividade.atualizar(input.projetoId(), input.clienteId(), input.descricao(), input.tipo(),
                input.status(), input.titulo(), input.data(), input.inicio(), input.termino(),
                input.local(), input.observacoes());
        return AtividadeMapper.toResponse(atividade);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        atividades.delete(obterDoTatuador(id, tatuadorId));
    }

    private void validarHorario(AtividadeInput input) {
        if ((input.inicio() == null) != (input.termino() == null)) {
            throw new BusinessException("Informe início e término juntos, ou deixe ambos vazios");
        }
    }

    private void validarTatuador(Integer tatuadorId, Integer tatuadorAutenticadoId) {
        if (!tatuadorId.equals(tatuadorAutenticadoId)) {
            throw new ResourceNotFoundException("Tatuador", tatuadorId);
        }
    }

    private Atividade obterDoTatuador(Integer id, Integer tatuadorId) {
        return atividades.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade", id));
    }

    private Tatuador obterTatuador(Integer id) {
        return tatuadores.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tatuador", id));
    }
}
