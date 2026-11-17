package com.kawz.tattoo.api.projeto.dto;

import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.api.contrato.dto.ContratoResponse;
import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.api.imagem.dto.ImagemResponse;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;

public record ProjetoDetalhadoResponse(ProjetoResponse projeto,
                                           java.util.List<SessaoResponse> sessoes,
                                           java.util.List<MovimentacaoResponse> movimentacoes,
                                           java.util.List<FollowUpResponse> followUps,
                                           java.util.List<ImagemResponse> imagens,
                                           CaracteristicasProjetoResponse caracteristicas,
                                           ContratoResponse contrato) {}
