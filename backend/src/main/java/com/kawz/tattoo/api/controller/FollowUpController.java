package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.FollowUpInput;
import com.kawz.tattoo.api.dto.ApiModels.FollowUpResponse;
import com.kawz.tattoo.domain.service.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/follow-ups")
public class FollowUpController {
    private final FollowUpService service;

    public FollowUpController(FollowUpService service) { this.service = service; }

    @GetMapping
    public List<FollowUpResponse> listar(
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate dataAte) {
        return service.listarFiltrado(projetoId, status, dataAte);
    }

    @GetMapping("/{id}")
    public FollowUpResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpResponse criar(@Valid @RequestBody FollowUpInput input) { return service.criar(input); }

    @PutMapping("/{id}")
    public FollowUpResponse atualizar(@PathVariable Integer id, @Valid @RequestBody FollowUpInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
