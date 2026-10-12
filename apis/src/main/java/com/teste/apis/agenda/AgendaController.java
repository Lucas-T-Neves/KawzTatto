package com.teste.apis.agenda;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teste.apis.agenda.dto.EventoRequestDTO;
import com.teste.apis.agenda.dto.EventoResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/agenda/eventos")
public class AgendaController {

    private final AgendaService service;

    public AgendaController(AgendaService service) {
        this.service = service;
    }

    /** Lista eventos no intervalo. `inicio` e `fim` em ISO 8601, opcionais. */
    @GetMapping
    public List<EventoResponseDTO> listar(
            @RequestParam(required = false) OffsetDateTime inicio,
            @RequestParam(required = false) OffsetDateTime fim) {
        return service.listar(inicio, fim);
    }

    @PostMapping
    public ResponseEntity<EventoResponseDTO> criar(@Valid @RequestBody EventoRequestDTO request) {
        EventoResponseDTO criado = service.criar(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public EventoResponseDTO atualizar(@PathVariable String id,
                                       @Valid @RequestBody EventoRequestDTO request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
