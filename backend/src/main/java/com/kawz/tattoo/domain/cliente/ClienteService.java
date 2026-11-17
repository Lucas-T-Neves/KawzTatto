package com.kawz.tattoo.domain.cliente;

import com.kawz.tattoo.api.cliente.dto.ClienteInput;
import com.kawz.tattoo.api.cliente.dto.ClienteResponse;
import com.kawz.tattoo.api.cliente.dto.ConversaResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasCliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kawz.tattoo.api.cliente.mapper.ClienteApiMapper.toResponse;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(com.kawz.tattoo.api.cliente.mapper.ClienteApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional(readOnly = true)
    public ConversaResponse conversa(Integer id) {
        Cliente cliente = obter(id);
        String url = cliente.getLinkConversa();
        return new ConversaResponse(id, url,
                url == null || url.isBlank() ? "NAO_CONFIGURADA" : "LINK_EXISTENTE");
    }

    @Transactional
    public ClienteResponse criar(ClienteInput input) {
        Cliente cliente = new Cliente(input.nome().trim(), input.telefone(),
                input.cpf(), input.instagram(), input.canalAquisicao(), input.linkConversa(), input.linkPasta(),
                input.localizacao(), input.tamanho(), input.ideia(),
                statusNovoCliente(input.status()));
        atualizarPreferencias(cliente.getPreferencias(), input);
        return toResponse(repository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Integer id, ClienteInput input) {
        Cliente cliente = obter(id);
        cliente.atualizarCadastro(input.nome().trim(), input.telefone(), input.cpf(), input.instagram(),
                input.canalAquisicao(), input.linkConversa(), input.linkPasta(),
                input.localizacao(), input.tamanho(), input.ideia(),
                statusAtualizado(input.status(), cliente.getStatus()));
        atualizarPreferencias(cliente.getPreferencias(), input);
        return toResponse(cliente);
    }

    @Transactional
    public void excluir(Integer id) { repository.delete(obter(id)); }

    private Cliente obter(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private void atualizarPreferencias(PreferenciasCliente preferencias, ClienteInput input) {
        preferencias.atualizarPreferencias(input.musicaFavorita(), input.comidaFavorita(),
                input.estiloTatuagemFavorita(), null, input.faixaOrcamento(), input.habitos(),
                input.disponibilidadeHorarios(), input.observacoesGerais());
    }

    private String statusNovoCliente(String statusInformado) {
        if (statusInformado == null || statusInformado.isBlank()) return "Interessado";
        return statusInformado;
    }

    private String statusAtualizado(String statusInformado, String statusAtual) {
        if (statusInformado == null || statusInformado.isBlank()) return statusAtual;
        return statusInformado;
    }
}
