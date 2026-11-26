package com.kawz.tattoo.domain.cliente;

import com.kawz.tattoo.api.cliente.ClienteMapper;
import com.kawz.tattoo.api.cliente.dto.ClienteInput;
import com.kawz.tattoo.api.cliente.dto.ClienteResponse;
import com.kawz.tattoo.api.cliente.dto.ConversaResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasCliente;
import com.kawz.tattoo.domain.tatuador.Tatuador;
import com.kawz.tattoo.domain.tatuador.TatuadorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clientes do tatuador autenticado. Toda operação recebe o id do dono (vindo do
 * token) e consulta apenas registros desse tatuador: um id de outro tatuador
 * resulta em 404, sem revelar que o registro existe.
 */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final TatuadorRepository tatuadores;

    public ClienteService(ClienteRepository clientes, TatuadorRepository tatuadores) {
        this.clientes = clientes;
        this.tatuadores = tatuadores;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Integer tatuadorId, Pageable pageable) {
        return clientes.findByTatuadorId(tatuadorId, pageable).map(ClienteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Integer id, Integer tatuadorId) {
        return ClienteMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional(readOnly = true)
    public ConversaResponse conversa(Integer id, Integer tatuadorId) {
        Cliente cliente = obterDoTatuador(id, tatuadorId);
        String url = cliente.getLinkConversa();
        return new ConversaResponse(id, url,
                url == null || url.isBlank() ? "NAO_CONFIGURADA" : "LINK_EXISTENTE");
    }

    @Transactional
    public ClienteResponse criar(ClienteInput input, Integer tatuadorId) {
        Cliente cliente = new Cliente(obterTatuador(tatuadorId), input.nome().trim(), input.telefone(),
                input.cpf(), input.instagram(), input.canalAquisicao(), input.linkConversa(), input.linkPasta(),
                input.localizacao(), input.tamanho(), input.ideia(),
                statusNovoCliente(input.status()));
        atualizarPreferencias(cliente.getPreferencias(), input);
        return ClienteMapper.toResponse(clientes.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Integer id, ClienteInput input, Integer tatuadorId) {
        Cliente cliente = obterDoTatuador(id, tatuadorId);
        cliente.atualizarCadastro(input.nome().trim(), input.telefone(), input.cpf(), input.instagram(),
                input.canalAquisicao(), input.linkConversa(), input.linkPasta(),
                input.localizacao(), input.tamanho(), input.ideia(),
                statusAtualizado(input.status(), cliente.getStatus()));
        atualizarPreferencias(cliente.getPreferencias(), input);
        return ClienteMapper.toResponse(cliente);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        clientes.delete(obterDoTatuador(id, tatuadorId));
    }

    /** Carrega o cliente garantindo que pertence ao tatuador autenticado. */
    @Transactional(readOnly = true)
    public Cliente obterDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Tatuador obterTatuador(Integer tatuadorId) {
        return tatuadores.findById(tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador", tatuadorId));
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
