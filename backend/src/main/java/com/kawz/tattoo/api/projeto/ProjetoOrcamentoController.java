package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.orcamento.dto.OrcamentoResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.orcamento.OrcamentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/orcamentos")
public class ProjetoOrcamentoController {

    private final OrcamentoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ProjetoOrcamentoController(OrcamentoService service,
                                      TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public List<OrcamentoResponse> listar(@PathVariable Integer projetoId) {
        return service.listarPorProjeto(projetoId, tatuadorAutenticado.id());
    }
}
