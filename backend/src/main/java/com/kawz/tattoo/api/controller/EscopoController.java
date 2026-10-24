package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.AtividadeResponse;
import com.kawz.tattoo.api.dto.ApiModels.FollowUpResponse;
import com.kawz.tattoo.domain.service.AtividadeService;
import com.kawz.tattoo.domain.service.FollowUpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EscopoController {
    private final AtividadeService atividades;
    private final FollowUpService followUps;

    public EscopoController(AtividadeService atividades, FollowUpService followUps) {
        this.atividades = atividades;
        this.followUps = followUps;
    }

    @GetMapping("/api/v1/tatuadores/{tatuadorId}/atividades")
    public List<AtividadeResponse> atividades(@PathVariable Integer tatuadorId) {
        return atividades.listarPorTatuador(tatuadorId);
    }

    @GetMapping("/api/v1/tatuadores/{tatuadorId}/follow-ups")
    public List<FollowUpResponse> followUpsPorTatuador(@PathVariable Integer tatuadorId) {
        return followUps.listarPorTatuador(tatuadorId);
    }

    @GetMapping("/api/v1/clientes/{clienteId}/follow-ups")
    public List<FollowUpResponse> followUpsPorCliente(@PathVariable Integer clienteId) {
        return followUps.listarPorCliente(clienteId);
    }
}
