package com.kawz.tattoo.api.cliente.mapper;

import com.kawz.tattoo.api.cliente.dto.ClienteResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasCliente;


public final class ClienteApiMapper {
    private ClienteApiMapper() {}

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
}
