package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.SessaoInput;
import com.kawz.tattoo.api.dto.ApiModels.SessaoRemarcarInput;
import com.kawz.tattoo.api.dto.ApiModels.SessaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.SessaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sessoes")
public class SessaoController {
    private final SessaoService service;

    public SessaoController(SessaoService service) { this.service = service; }

    @GetMapping
    public Page<SessaoResponse> listar(
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) LocalDate dataApos,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        Pageable pageable = Paginacao.de(pagina, tamanho);
        if (dataApos != null) return service.listarPorDataApos(dataApos.atStartOfDay(), pageable);
        return projetoId == null ? service.listar(pageable) : service.listarPorProjeto(projetoId, pageable);
    }

    @GetMapping("/{id}")
    public SessaoResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse criar(@Valid @RequestBody SessaoInput input) { return service.criar(input); }

    @PutMapping("/{id}")
    public SessaoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody SessaoInput input) {
        return service.atualizar(id, input);
    }

    @PatchMapping("/{id}/remarcar")
    public SessaoResponse remarcar(@PathVariable Integer id,
                                   @Valid @RequestBody SessaoRemarcarInput input) {
        return service.remarcar(id, input.dataHorario());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
