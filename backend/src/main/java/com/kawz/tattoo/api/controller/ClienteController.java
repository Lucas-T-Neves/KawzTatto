package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.ClienteInput;
import com.kawz.tattoo.api.dto.ApiModels.ClienteResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping
    public Page<ClienteResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@Valid @RequestBody ClienteInput input) { return service.criar(input); }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ClienteInput input) {
        return service.atualizar(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) { service.excluir(id); }
}
