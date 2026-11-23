package com.kawz.tattoo.domain.preferenciascliente;

import com.kawz.tattoo.api.preferenciascliente.PreferenciasClienteMapper;
import com.kawz.tattoo.api.preferenciascliente.dto.PreferenciasClienteInput;
import com.kawz.tattoo.api.preferenciascliente.dto.PreferenciasClienteResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Preferências do cliente: o acesso é autorizado pela posse do cliente. */
@Service
public class PreferenciasClienteService {

    private final ClienteRepository clientes;

    public PreferenciasClienteService(ClienteRepository clientes) { this.clientes = clientes; }

    @Transactional(readOnly = true)
    public PreferenciasClienteResponse buscar(Integer clienteId, Integer tatuadorId) {
        Cliente cliente = obterClienteDoTatuador(clienteId, tatuadorId);
        return PreferenciasClienteMapper.toResponse(cliente.getPreferencias(), clienteId);
    }

    @Transactional
    public PreferenciasClienteResponse atualizar(Integer clienteId, PreferenciasClienteInput input,
                                                 Integer tatuadorId) {
        Cliente cliente = obterClienteDoTatuador(clienteId, tatuadorId);
        PreferenciasCliente preferencias = cliente.getPreferencias();
        preferencias.atualizarPreferencias(input.musicaFavorita(), input.comidaFavorita(),
                input.estiloTatuagemFavorita(), input.faixaMediaOrcamento(), input.faixaOrcamento(), input.habitos(),
                input.disponibilidadeHorarios(), input.observacoesGerais());
        return PreferenciasClienteMapper.toResponse(preferencias, clienteId);
    }

    private Cliente obterClienteDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
