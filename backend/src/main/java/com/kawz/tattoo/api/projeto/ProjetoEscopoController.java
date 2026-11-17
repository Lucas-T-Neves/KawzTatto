package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.contrato.dto.ContratoResponse;
import com.kawz.tattoo.api.projeto.dto.ProjetoDetalhadoResponse;
import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.api.imagem.dto.ImagemResponse;
import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.caracteristicasprojeto.CaracteristicasProjetoService;
import com.kawz.tattoo.domain.contrato.ContratoService;
import com.kawz.tattoo.domain.followup.FollowUpService;
import com.kawz.tattoo.domain.imagem.ImagemService;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoService;
import com.kawz.tattoo.domain.projeto.ProjetoService;
import com.kawz.tattoo.domain.sessao.SessaoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}")
public class ProjetoEscopoController {
    private static final int MAX_DETALHES = 1000;
    private final ProjetoService projetos;
    private final SessaoService sessoes;
    private final MovimentacaoService movimentacoes;
    private final FollowUpService followUps;
    private final ImagemService imagens;
    private final CaracteristicasProjetoService caracteristicas;
    private final ContratoService contratos;

    public ProjetoEscopoController(ProjetoService projetos, SessaoService sessoes,
                                   MovimentacaoService movimentacoes, FollowUpService followUps,
                                   ImagemService imagens, CaracteristicasProjetoService caracteristicas,
                                   ContratoService contratos) {
        this.projetos = projetos;
        this.sessoes = sessoes;
        this.movimentacoes = movimentacoes;
        this.followUps = followUps;
        this.imagens = imagens;
        this.caracteristicas = caracteristicas;
        this.contratos = contratos;
    }

    @GetMapping("/sessoes")
    public Page<SessaoResponse> listarSessoes(@PathVariable Integer projetoId,
                                               @RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "20") int tamanho) {
        return sessoes.listarPorProjeto(projetoId, Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/follow-ups")
    public List<FollowUpResponse> listarFollowUps(@PathVariable Integer projetoId) {
        return followUps.listarPorProjeto(projetoId);
    }

    @GetMapping("/contrato")
    public ContratoResponse buscarContrato(@PathVariable Integer projetoId) {
        return contratos.buscarPorProjeto(projetoId);
    }

    @GetMapping("/detalhado")
    public ProjetoDetalhadoResponse detalhado(@PathVariable Integer projetoId) {
        ProjetoResponse projeto = projetos.buscar(projetoId);
        Page<SessaoResponse> sessoesDoProjeto = sessoes.listarPorProjeto(projetoId, Paginacao.de(0, MAX_DETALHES));
        Page<MovimentacaoResponse> movimentacoesDoProjeto = movimentacoes.listarPorProjeto(projetoId, Paginacao.de(0, MAX_DETALHES));
        return new ProjetoDetalhadoResponse(
                projeto,
                sessoesDoProjeto.getContent(),
                movimentacoesDoProjeto.getContent(),
                followUps.listarPorProjeto(projetoId),
                imagens.listarPorProjeto(projetoId),
                caracteristicas.buscar(projetoId),
                contratos.buscarPorProjeto(projetoId));
    }
}
