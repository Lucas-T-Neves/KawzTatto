package com.kawz.tattoo.api.followup;

import com.kawz.tattoo.api.followup.dto.FollowUpInput;
import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.followup.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/follow-ups")
public class FollowUpController {

    private final FollowUpService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public FollowUpController(FollowUpService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    /** Array simples (contrato do frontend): não trocar por Page. */
    @GetMapping
    public List<FollowUpResponse> listar(
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate dataAte) {
        return service.listarFiltrado(projetoId, status, dataAte, tatuadorAutenticado.id());
    }

    @GetMapping("/{id}")
    public FollowUpResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpResponse criar(@Valid @RequestBody FollowUpInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public FollowUpResponse atualizar(@PathVariable Integer id, @Valid @RequestBody FollowUpInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
