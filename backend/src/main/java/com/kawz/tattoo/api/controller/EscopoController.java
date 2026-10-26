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
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class EscopoController {
    private final AtividadeService atividades;
    private final FollowUpService followUps;

    public EscopoController(AtividadeService atividades, FollowUpService followUps) {
        this.atividades = atividades;
        this.followUps = followUps;
    }

    @GetMapping("/api/v1/tatuadores/{tatuadorId}/atividades")
    public List<AtividadeResponse> atividades(@PathVariable Integer tatuadorId,
                                                @RequestParam(required = false) LocalDate data) {
        return data == null ? atividades.listarPorTatuador(tatuadorId)
                : atividades.listarPorTatuadorEData(tatuadorId, data);
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
