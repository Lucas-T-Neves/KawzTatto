package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.FollowUpInput;
import com.kawz.tattoo.api.dto.ApiModels.FollowUpResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Cliente;
import com.kawz.tattoo.domain.model.FollowUp;
import com.kawz.tattoo.domain.repository.ClienteRepository;
import com.kawz.tattoo.domain.repository.FollowUpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class FollowUpService {
    private final FollowUpRepository followUps;
    private final ClienteRepository clientes;

    public FollowUpService(FollowUpRepository followUps, ClienteRepository clientes) {
        this.followUps = followUps;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarPorCliente(Integer clienteId) {
        return followUps.findByClienteIdOrderByDataPrevistaAsc(clienteId).stream()
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarPorTatuador(Integer tatuadorId) {
        return followUps.findByTatuadorId(tatuadorId).stream()
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listar() {
        return followUps.findAll().stream().map(com.kawz.tattoo.api.dto.ApiMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarFiltrado(Integer projetoId, String status, java.time.LocalDate dataAte) {
        List<FollowUp> encontrados;
        if (projetoId != null) encontrados = followUps.findByProjetoId(projetoId);
        else if (status != null && !status.isBlank()) encontrados = followUps.findByStatusContatoIgnoreCaseOrderByDataPrevistaAsc(status);
        else if (dataAte != null) encontrados = followUps.findByDataPrevistaLessThanEqualOrderByDataPrevistaAsc(dataAte);
        else return listar();
        if (status != null && !status.isBlank() && projetoId != null) {
            encontrados = encontrados.stream().filter(f -> status.equalsIgnoreCase(f.getStatusContato())).toList();
        }
        if (dataAte != null && projetoId != null) {
            encontrados = encontrados.stream().filter(f -> f.getDataPrevista() != null && !f.getDataPrevista().isAfter(dataAte)).toList();
        }
        return encontrados.stream().map(com.kawz.tattoo.api.dto.ApiMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarPorProjeto(Integer projetoId) {
        return followUps.findByProjetoId(projetoId).stream()
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FollowUpResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional
    public FollowUpResponse criar(FollowUpInput input) {
        return toResponse(followUps.save(new FollowUp(obterCliente(input.clienteId()), input.tipoContato(),
                input.statusContato(), input.dataPrevista(), input.dataRealizacao(),
                input.descricaoObservacao(), input.proximaAcao())));
    }

    @Transactional
    public FollowUpResponse atualizar(Integer id, FollowUpInput input) {
        FollowUp followUp = obter(id);
        followUp.vincularCliente(obterCliente(input.clienteId()));
        followUp.atualizar(input.tipoContato(), input.statusContato(), input.dataPrevista(),
                input.dataRealizacao(), input.descricaoObservacao(), input.proximaAcao());
        return toResponse(followUp);
    }

    @Transactional
    public void excluir(Integer id) { followUps.delete(obter(id)); }

    private Cliente obterCliente(Integer id) {
        return clientes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private FollowUp obter(Integer id) {
        return followUps.findById(id).orElseThrow(() -> new ResourceNotFoundException("FollowUp", id));
    }
}
