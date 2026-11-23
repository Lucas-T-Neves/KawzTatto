package com.kawz.tattoo.domain.anamnese;

import com.kawz.tattoo.api.anamnese.AnamneseMapper;
import com.kawz.tattoo.api.anamnese.dto.AnamneseInput;
import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Anamneses dos clientes do tatuador autenticado. O proprietário é o cliente.
 */
@Service
public class AnamneseService {

    private final AnamneseRepository anamneses;
    private final ClienteRepository clientes;

    public AnamneseService(AnamneseRepository anamneses, ClienteRepository clientes) {
        this.anamneses = anamneses;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public List<AnamneseResponse> listarPorCliente(Integer clienteId, Integer tatuadorId) {
        obterClienteDoTatuador(clienteId, tatuadorId);
        // stream().map().toList() simplifica a conversão de cada Anamnese
        // para AnamneseResponse e a reunião dos DTOs em uma lista.
        return anamneses.findByClienteIdAndCliente_Tatuador_IdOrderByIdDesc(clienteId, tatuadorId).stream()
                .map(AnamneseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnamneseResponse buscar(Integer id, Integer tatuadorId) {
        return AnamneseMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public AnamneseResponse criar(AnamneseInput input, Integer tatuadorId) {
        Cliente cliente = obterClienteDoTatuador(input.clienteId(), tatuadorId);
        return AnamneseMapper.toResponse(anamneses.save(
                new Anamnese(cliente, input.dadosSaude(), input.preparacaoTatuagem())));
    }

    @Transactional
    public AnamneseResponse atualizar(Integer id, AnamneseInput input, Integer tatuadorId) {
        Anamnese anamnese = obterDoTatuador(id, tatuadorId);
        if (!anamnese.getCliente().getId().equals(input.clienteId())) {
            throw new ResourceNotFoundException("Anamnese", id);
        }
        anamnese.atualizarInformacoes(input.dadosSaude(), input.preparacaoTatuagem());
        return AnamneseMapper.toResponse(anamnese);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        anamneses.delete(obterDoTatuador(id, tatuadorId));
    }

    private Cliente obterClienteDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Anamnese obterDoTatuador(Integer id, Integer tatuadorId) {
        return anamneses.findByIdAndCliente_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Anamnese", id));
    }
}
