package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoService;
import com.kawz.tattoo.api.web.PaginaResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/movimentacoes")
public class ProjetoMovimentacaoController {

    private final MovimentacaoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ProjetoMovimentacaoController(MovimentacaoService service,
                                         TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public PaginaResponse<MovimentacaoResponse> listar(@PathVariable Integer projetoId,
                                             @RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(service.listarPorProjeto(projetoId, tatuadorAutenticado.id(), Paginacao.de(pagina, tamanho)));
    }

    @GetMapping("/{id}")
    public MovimentacaoResponse buscar(@PathVariable Integer projetoId, @PathVariable Integer id) {
        return service.buscarPorProjeto(projetoId, id, tatuadorAutenticado.id());
    }
}
