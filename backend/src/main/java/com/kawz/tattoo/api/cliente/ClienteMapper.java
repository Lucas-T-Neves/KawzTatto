package com.kawz.tattoo.api.cliente;

import com.kawz.tattoo.api.cliente.dto.ClienteInput;
import com.kawz.tattoo.api.cliente.dto.ClienteResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasCliente;
import com.kawz.tattoo.domain.tatuador.Tatuador;


// bom padrao. talvez implementar em todos mappers
public final class ClienteMapper {
    private ClienteMapper() {}

    public static ClienteResponse toResponse(Cliente value) {
        PreferenciasCliente preferencias = value.getPreferencias();
        return new ClienteResponse(value.getId(), value.getNome(), value.getTelefone(),
                value.getCpf(), value.getInstagram(), value.getCanalAquisicao(), value.getLinkConversa(), value.getLinkPasta(),
                value.getLocalizacao(), value.getTamanho(), value.getIdeia(),
                preferencias == null ? null : preferencias.getMusicaFavorita(),
                preferencias == null ? null : preferencias.getComidaFavorita(),
                preferencias == null ? null : preferencias.getEstiloTatuagemFavorita(),
                preferencias == null ? null : preferencias.getFaixaOrcamento(),
                preferencias == null ? null : preferencias.getHabitos(),
                preferencias == null ? null : preferencias.getDisponibilidadeHorarios(),
                preferencias == null ? null : preferencias.getObservacoesGerais(),
                value.getStatus(), value.getDataCriacao());
    }

    public static Cliente toEntity(ClienteInput value, Tatuador tatuador) {

        return new Cliente(tatuador, value.nome(),
                value.telefone(),
                value.cpf(),
                value.instagram(),
                value.canalAquisicao(),
                value.linkConversa(),
                value.linkPasta(),
                value.localizacao(),
                value.tamanho(),
                value.ideia(),
                value.status());
    }
}
