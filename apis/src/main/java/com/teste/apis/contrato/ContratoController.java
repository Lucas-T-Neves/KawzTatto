package com.teste.apis.contrato;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contratos")
public class ContratoController {

    private final ContratoService service;

    public ContratoController(ContratoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ContratoResponseDTO> criar(@Valid @RequestBody ContratoRequestDTO request,
                                                     UriComponentsBuilder uriBuilder) {
        ContratoResponseDTO criado = service.criar(request);
        URI location = uriBuilder.path("/contratos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/{id}")
    public ContratoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping
    public List<ContratoResponseDTO> listar() {
        return service.listar();
    }
}
