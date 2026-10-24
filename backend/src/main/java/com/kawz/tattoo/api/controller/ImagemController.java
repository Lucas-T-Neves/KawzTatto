package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.ImagemInput;
import com.kawz.tattoo.api.dto.ApiModels.ImagemResponse;
import com.kawz.tattoo.domain.service.ImagemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ImagemController {
    private final ImagemService service;

    public ImagemController(ImagemService service) { this.service = service; }

    @GetMapping("/api/v1/projetos/{projetoId}/imagens")
    public List<ImagemResponse> listarPorProjeto(@PathVariable Integer projetoId) {
        return service.listarPorProjeto(projetoId);
    }

    @GetMapping("/api/v1/imagens/{id}")
    public ImagemResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping("/api/v1/imagens")
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemResponse criar(@Valid @RequestBody ImagemInput input) { return service.criar(input); }

    @PutMapping("/api/v1/imagens/{id}")
    public ImagemResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ImagemInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/api/v1/imagens/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
