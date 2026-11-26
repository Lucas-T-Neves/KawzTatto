package com.kawz.tattoo.domain.followup;

import com.kawz.tattoo.api.followup.FollowUpMapper;
import com.kawz.tattoo.api.followup.dto.FollowUpInput;
import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Follow-ups dos clientes do tatuador autenticado. O vínculo de propriedade é o
 * cliente (cliente.tatuador_id), inclusive quando o filtro é por projeto.
 */
@Service
public class FollowUpService {

    private final FollowUpRepository followUps;
    private final ClienteRepository clientes;
    private final ProjetoRepository projetos;

    public FollowUpService(FollowUpRepository followUps, ClienteRepository clientes, ProjetoRepository projetos) {
        this.followUps = followUps;
        this.clientes = clientes;
        this.projetos = projetos;
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarPorCliente(Integer clienteId, Integer tatuadorId) {
        obterClienteDoTatuador(clienteId, tatuadorId);
         return followUps.findByClienteIdAndCliente_Tatuador_IdOrderByDataPrevistaAsc(clienteId, tatuadorId).stream()
                .map(FollowUpMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarPorTatuador(Integer tatuadorId) {
         return followUps.findByCliente_Tatuador_Id(tatuadorId).stream()
                .map(FollowUpMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listar(Integer tatuadorId) {
        return listarPorTatuador(tatuadorId);
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarFiltrado(Integer projetoId, String status, LocalDate dataAte,
                                                 Integer tatuadorId) {
        List<FollowUp> encontrados;
        if (projetoId != null) {
            obterProjetoDoTatuador(projetoId, tatuadorId);
            encontrados = followUps.findByProjetoIdDoTatuador(projetoId, tatuadorId);
        } else if (status != null && !status.isBlank()) {
            encontrados = followUps.findByStatusContatoIgnoreCaseAndCliente_Tatuador_IdOrderByDataPrevistaAsc(
                    status, tatuadorId);
        } else if (dataAte != null) {
            encontrados = followUps.findByDataPrevistaLessThanEqualAndCliente_Tatuador_IdOrderByDataPrevistaAsc(
                    dataAte, tatuadorId);
        } else {
            return listar(tatuadorId);
        }
        if (status != null && !status.isBlank() && projetoId != null) {
            encontrados = encontrados.stream().filter(f -> status.equalsIgnoreCase(f.getStatusContato())).toList();
        }
        if (dataAte != null && projetoId != null) {
            encontrados = encontrados.stream()
                    .filter(f -> f.getDataPrevista() != null && !f.getDataPrevista().isAfter(dataAte))
                    .toList();
        }
         return encontrados.stream().map(FollowUpMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> listarPorProjeto(Integer projetoId, Integer tatuadorId) {
        obterProjetoDoTatuador(projetoId, tatuadorId);
         return followUps.findByProjetoIdDoTatuador(projetoId, tatuadorId).stream()
                .map(FollowUpMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FollowUpResponse buscar(Integer id, Integer tatuadorId) {
        return FollowUpMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public FollowUpResponse criar(FollowUpInput input, Integer tatuadorId) {
        return FollowUpMapper.toResponse(followUps.save(new FollowUp(
                obterClienteDoTatuador(input.clienteId(), tatuadorId), input.tipoContato(),
                input.statusContato(), input.dataPrevista(), input.dataRealizacao(),
                input.descricaoObservacao(), input.proximaAcao())));
    }

    @Transactional
    public FollowUpResponse atualizar(Integer id, FollowUpInput input, Integer tatuadorId) {
        FollowUp followUp = obterDoTatuador(id, tatuadorId);
        followUp.vincularCliente(obterClienteDoTatuador(input.clienteId(), tatuadorId));
        followUp.atualizar(input.tipoContato(), input.statusContato(), input.dataPrevista(),
                input.dataRealizacao(), input.descricaoObservacao(), input.proximaAcao());
        return FollowUpMapper.toResponse(followUp);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        followUps.delete(obterDoTatuador(id, tatuadorId));
    }

    private Cliente obterClienteDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private void obterProjetoDoTatuador(Integer projetoId, Integer tatuadorId) {
        projetos.findByIdAndTatuadorId(projetoId, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", projetoId));
    }

    private FollowUp obterDoTatuador(Integer id, Integer tatuadorId) {
        return followUps.findByIdAndCliente_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("FollowUp", id));
    }
}
