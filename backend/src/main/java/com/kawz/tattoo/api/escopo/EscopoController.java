package com.kawz.tattoo.api.escopo;

import com.kawz.tattoo.api.atividade.dto.AtividadeResponse;
import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.atividade.AtividadeService;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.followup.FollowUpService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Rotas por tatuador usadas pela agenda. O {@code tatuadorId} do caminho é
 * mantido porque o frontend o envia, mas só é aceito quando é o próprio
 * tatuador autenticado; caso contrário responde 404 (não 403), sem revelar
 * a existência do recurso alheio.
 */
@RestController
public class EscopoController {

    private final AtividadeService atividades;
    private final FollowUpService followUps;
    private final TatuadorAutenticado tatuadorAutenticado;

    public EscopoController(AtividadeService atividades, FollowUpService followUps,
                            TatuadorAutenticado tatuadorAutenticado) {
        this.atividades = atividades;
        this.followUps = followUps;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping("/api/v1/tatuadores/{tatuadorId}/atividades")
    public List<AtividadeResponse> atividades(@PathVariable Integer tatuadorId,
                                              @RequestParam(required = false) LocalDate data) {
        Integer autenticado = tatuadorAutenticado.id();
        return data == null ? atividades.listarPorTatuador(tatuadorId, autenticado)
                : atividades.listarPorTatuadorEData(tatuadorId, data, autenticado);
    }

    @GetMapping("/api/v1/tatuadores/{tatuadorId}/follow-ups")
    public List<FollowUpResponse> followUpsPorTatuador(@PathVariable Integer tatuadorId) {
        if (!tatuadorId.equals(tatuadorAutenticado.id())) {
            throw new ResourceNotFoundException("Tatuador", tatuadorId);
        }
        return followUps.listarPorTatuador(tatuadorId);
    }

    @GetMapping("/api/v1/clientes/{clienteId}/follow-ups")
    public List<FollowUpResponse> followUpsPorCliente(@PathVariable Integer clienteId) {
        return followUps.listarPorCliente(clienteId, tatuadorAutenticado.id());
    }
}
