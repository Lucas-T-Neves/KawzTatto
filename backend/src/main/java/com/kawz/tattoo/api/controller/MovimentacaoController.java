package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoInput;
import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoResponse;
import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoResumoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movimentacoes")
public class MovimentacaoController {
    private final MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) { this.service = service; }

    @GetMapping
    public Page<MovimentacaoResponse> listar(
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        Pageable pageable = Paginacao.de(pagina, tamanho);
        return service.listar(mes, status, projetoId, clienteId, pageable);
    }

    @GetMapping("/resumo")
    public MovimentacaoResumoResponse resumo(@RequestParam String mes) {
        return service.resumo(mes);
    }

    @GetMapping("/{id}")
    public MovimentacaoResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse criar(@Valid @RequestBody MovimentacaoInput input) {
        return service.criar(input);
    }

    @PutMapping("/{id}")
    public MovimentacaoResponse atualizar(@PathVariable Integer id,
                                          @Valid @RequestBody MovimentacaoInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
