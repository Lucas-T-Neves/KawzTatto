package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.OrcamentoResponse;
import com.kawz.tattoo.domain.service.OrcamentoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/orcamentos")
public class ProjetoOrcamentoController {
    private final OrcamentoService service;

    public ProjetoOrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrcamentoResponse> listar(@PathVariable Integer projetoId) {
        return service.listarPorProjeto(projetoId);
    }
}
