package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.AtividadeInput;
import com.kawz.tattoo.api.dto.ApiModels.AtividadeResponse;
import com.kawz.tattoo.domain.service.AtividadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/atividades")
public class AtividadeController {
    private final AtividadeService service;

    public AtividadeController(AtividadeService service) { this.service = service; }

    @GetMapping("/{id}")
    public AtividadeResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtividadeResponse criar(@Valid @RequestBody AtividadeInput input) { return service.criar(input); }

    @PutMapping("/{id}")
    public AtividadeResponse atualizar(@PathVariable Integer id, @Valid @RequestBody AtividadeInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
