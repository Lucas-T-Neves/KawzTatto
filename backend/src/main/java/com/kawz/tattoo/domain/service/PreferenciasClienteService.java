package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.PreferenciasClienteInput;
import com.kawz.tattoo.api.dto.ApiModels.PreferenciasClienteResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Cliente;
import com.kawz.tattoo.domain.model.PreferenciasCliente;
import com.kawz.tattoo.domain.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class PreferenciasClienteService {
    private final ClienteRepository clientes;

    public PreferenciasClienteService(ClienteRepository clientes) { this.clientes = clientes; }

    @Transactional(readOnly = true)
    public PreferenciasClienteResponse buscar(Integer clienteId) {
        Cliente cliente = obterCliente(clienteId);
        return toResponse(cliente.getPreferencias(), clienteId);
    }

    @Transactional
    public PreferenciasClienteResponse atualizar(Integer clienteId, PreferenciasClienteInput input) {
        Cliente cliente = obterCliente(clienteId);
        PreferenciasCliente preferencias = cliente.getPreferencias();
        preferencias.atualizarPreferencias(input.musicaFavorita(), input.comidaFavorita(),
                input.estiloTatuagemFavorita(), input.faixaMediaOrcamento(), input.habitos(),
                input.disponibilidadeHorarios(), input.observacoesGerais());
        return toResponse(preferencias, clienteId);
    }

    private Cliente obterCliente(Integer id) {
        return clientes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
