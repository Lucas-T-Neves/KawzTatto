package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.MovimentacaoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/movimentacoes")
public class ProjetoMovimentacaoController {
    private final MovimentacaoService service;

    public ProjetoMovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<MovimentacaoResponse> listar(@PathVariable Integer projetoId,
                                             @RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarPorProjeto(projetoId, Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public MovimentacaoResponse buscar(@PathVariable Integer projetoId, @PathVariable Integer id) {
        return service.buscarPorProjeto(projetoId, id);
    }
}
