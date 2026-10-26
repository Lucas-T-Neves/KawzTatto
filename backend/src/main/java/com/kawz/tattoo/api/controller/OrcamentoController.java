package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.OrcamentoInput;
import com.kawz.tattoo.api.dto.ApiModels.OrcamentoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orcamentos")
public class OrcamentoController {
    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<OrcamentoResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(status, projetoId, clienteId, Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public OrcamentoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoResponse criar(@Valid @RequestBody OrcamentoInput input) {
        return service.criar(input);
    }

    @PutMapping("/{id}")
    public OrcamentoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody OrcamentoInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}
