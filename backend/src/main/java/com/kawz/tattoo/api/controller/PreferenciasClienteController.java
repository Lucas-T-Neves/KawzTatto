package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.PreferenciasClienteInput;
import com.kawz.tattoo.api.dto.ApiModels.PreferenciasClienteResponse;
import com.kawz.tattoo.domain.service.PreferenciasClienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes/{clienteId}/preferencias")
public class PreferenciasClienteController {
    private final PreferenciasClienteService service;

    public PreferenciasClienteController(PreferenciasClienteService service) { this.service = service; }

    @GetMapping
    public PreferenciasClienteResponse buscar(@PathVariable Integer clienteId) {
        return service.buscar(clienteId);
    }

    @PutMapping
    public PreferenciasClienteResponse atualizar(@PathVariable Integer clienteId,
                                                  @Valid @RequestBody PreferenciasClienteInput input) {
        return service.atualizar(clienteId, input);
    }
}
