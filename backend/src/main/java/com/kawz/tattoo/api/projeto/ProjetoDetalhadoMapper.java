package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.caracteristicasprojeto.CaracteristicasProjetoMapper;
import com.kawz.tattoo.api.contrato.ContratoMapper;
import com.kawz.tattoo.api.followup.FollowUpMapper;
import com.kawz.tattoo.api.imagem.ImagemMapper;
import com.kawz.tattoo.api.movimentacao.MovimentacaoMapper;
import com.kawz.tattoo.api.projeto.dto.ProjetoDetalhadoResponse;
import com.kawz.tattoo.api.sessao.SessaoMapper;
import com.kawz.tattoo.application.projeto.ProjetoDetalhadoResult;

/** Converte o resultado do caso de uso em resposta HTTP (camada de API). */
public final class ProjetoDetalhadoMapper {

    private ProjetoDetalhadoMapper() {}

    public static ProjetoDetalhadoResponse toResponse(ProjetoDetalhadoResult resultado) {
        var projeto = resultado.projeto();
        // Os stream().map().toList() abaixo simplificam quatro conversões
        // repetitivas: cada entidade de domínio vira o DTO correspondente.
        return new ProjetoDetalhadoResponse(
                ProjetoMapper.toResponse(projeto, resultado.recebido(), resultado.sinalPago(),
                        resultado.referencia()),
                resultado.sessoes().stream().map(SessaoMapper::toResponse).toList(),
                resultado.movimentacoes().stream().map(MovimentacaoMapper::toResponse).toList(),
                resultado.followUps().stream().map(FollowUpMapper::toResponse).toList(),
                resultado.imagens().stream().map(ImagemMapper::toResponse).toList(),
                CaracteristicasProjetoMapper.toResponse(projeto.getCaracteristicas(), projeto.getId()),
                ContratoMapper.toResponse(projeto.getContrato()));
    }
}
