package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.contrato.dto.ContratoResponse;
import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.api.projeto.dto.ProjetoDetalhadoResponse;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.projeto.BuscarProjetoDetalhadoUseCase;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.contrato.ContratoService;
import com.kawz.tattoo.domain.followup.FollowUpService;
import com.kawz.tattoo.domain.sessao.SessaoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rotas aninhadas de projeto. O endpoint {@code /detalhado} apenas adapta HTTP:
 * toda a composição está em {@link BuscarProjetoDetalhadoUseCase}.
 */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}")
public class ProjetoEscopoController {

    private final BuscarProjetoDetalhadoUseCase buscarDetalhado;
    private final SessaoService sessoes;
    private final FollowUpService followUps;
    private final ContratoService contratos;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ProjetoEscopoController(BuscarProjetoDetalhadoUseCase buscarDetalhado, SessaoService sessoes,
                                   FollowUpService followUps, ContratoService contratos,
                                   TatuadorAutenticado tatuadorAutenticado) {
        this.buscarDetalhado = buscarDetalhado;
        this.sessoes = sessoes;
        this.followUps = followUps;
        this.contratos = contratos;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping("/sessoes")
    public Page<SessaoResponse> listarSessoes(@PathVariable Integer projetoId,
                                              @RequestParam(defaultValue = "0") int pagina,
                                              @RequestParam(defaultValue = "20") int tamanho) {
        return sessoes.listarPorProjeto(projetoId, tatuadorAutenticado.id(), Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/follow-ups")
    public List<FollowUpResponse> listarFollowUps(@PathVariable Integer projetoId) {
        return followUps.listarPorProjeto(projetoId, tatuadorAutenticado.id());
    }

    @GetMapping("/contrato")
    public ContratoResponse buscarContrato(@PathVariable Integer projetoId) {
        return contratos.buscarPorProjeto(projetoId, tatuadorAutenticado.id());
    }

    @GetMapping("/detalhado")
    public ProjetoDetalhadoResponse detalhado(@PathVariable Integer projetoId) {
        return ProjetoDetalhadoMapper.toResponse(
                buscarDetalhado.executar(projetoId, tatuadorAutenticado.id()));
    }
}
