package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.AnamneseInput;
import com.kawz.tattoo.api.dto.ApiModels.AnamneseResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Anamnese;
import com.kawz.tattoo.domain.model.Cliente;
import com.kawz.tattoo.domain.repository.AnamneseRepository;
import com.kawz.tattoo.domain.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class AnamneseService {
    private final AnamneseRepository anamneses;
    private final ClienteRepository clientes;

    public AnamneseService(AnamneseRepository anamneses, ClienteRepository clientes) {
        this.anamneses = anamneses;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public List<AnamneseResponse> listarPorCliente(Integer clienteId) {
        return anamneses.findByClienteIdOrderByIdDesc(clienteId).stream()
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AnamneseResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional
    public AnamneseResponse criar(AnamneseInput input) {
        Cliente cliente = obterCliente(input.clienteId());
        return toResponse(anamneses.save(new Anamnese(cliente, input.dadosSaude(), input.preparacaoTatuagem())));
    }

    @Transactional
    public AnamneseResponse atualizar(Integer id, AnamneseInput input) {
        Anamnese anamnese = obter(id);
        if (!anamnese.getCliente().getId().equals(input.clienteId())) {
            throw new ResourceNotFoundException("Anamnese", id);
        }
        anamnese.atualizarInformacoes(input.dadosSaude(), input.preparacaoTatuagem());
        return toResponse(anamnese);
    }

    @Transactional
    public void excluir(Integer id) { anamneses.delete(obter(id)); }

    private Cliente obterCliente(Integer id) {
        return clientes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Anamnese obter(Integer id) {
        return anamneses.findById(id).orElseThrow(() -> new ResourceNotFoundException("Anamnese", id));
    }
}
