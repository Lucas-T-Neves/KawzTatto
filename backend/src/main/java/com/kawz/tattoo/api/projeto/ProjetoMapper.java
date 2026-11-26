package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.domain.imagem.Imagem;
import com.kawz.tattoo.domain.projeto.Projeto;

import java.math.BigDecimal;

public final class ProjetoMapper {
    private ProjetoMapper() {}

    public static ProjetoResponse toResponse(Projeto value, BigDecimal recebido, boolean sinalPago, Imagem referencia) {
        var caracteristicas = value.getCaracteristicas();
        return new ProjetoResponse(value.getId(), value.getTatuador().getId(), value.getCliente().getId(),
                value.getNome(), value.getDescricao(), value.getLinkPasta(),
                value.getStatusProjeto(), value.getValorTotal(), value.getValorSinal(),
                value.calcularSaldo(recebido), recebido, sinalPago, value.getQuantidadeSessoes(),
                value.getDataValidade(), value.getLembretePre(), value.getLembretePost(),
                caracteristicas.getIdeiaExpectativa(), caracteristicas.getEstiloArte(),
                caracteristicas.getLocalizacaoCorpo(), caracteristicas.getTamanho(),
                caracteristicas.getDificuldade(), caracteristicas.getCores(), caracteristicas.getComplexidade(),
                referencia == null ? null : referencia.getId(),
                referencia == null ? null : nomeImagem(referencia),
                referencia == null ? null : referencia.getUrl(),
                caracteristicas.getId());
    }

    private static String nomeImagem(Imagem imagem) {
        String publicId = imagem.getPublicId();
        if (publicId == null || publicId.isBlank()) return null;
        int separador = publicId.lastIndexOf('/');
        String nome = separador >= 0 ? publicId.substring(separador + 1) : publicId;
        return nome.isBlank() ? publicId : nome;
    }
}
