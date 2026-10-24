package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.ProjetoInput;
import com.kawz.tattoo.api.dto.ApiModels.ProjetoResponse;
import com.kawz.tattoo.api.dto.ApiModels.StatusRequest;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos")
public class ProjetoController {
    private final ProjetoService service;

    public ProjetoController(ProjetoService service) { this.service = service; }

    @GetMapping
    public Page<ProjetoResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ProjetoResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoResponse criar(@Valid @RequestBody ProjetoInput input) { return service.criar(input); }

    @PutMapping("/{id}")
    public ProjetoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ProjetoInput input) {
        return service.atualizar(id, input);
    }

    @PatchMapping("/{id}/status")
    public ProjetoResponse alterarStatus(@PathVariable Integer id, @Valid @RequestBody StatusRequest input) {
        return service.alterarStatus(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
