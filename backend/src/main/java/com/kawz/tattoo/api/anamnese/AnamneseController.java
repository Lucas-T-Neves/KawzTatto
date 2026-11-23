package com.kawz.tattoo.api.anamnese;

import com.kawz.tattoo.api.anamnese.dto.AnamneseInput;
import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.anamnese.AnamneseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AnamneseController {

    private final AnamneseService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public AnamneseController(AnamneseService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping("/api/v1/clientes/{clienteId}/anamneses")
    public List<AnamneseResponse> listarPorCliente(@PathVariable Integer clienteId) {
        return service.listarPorCliente(clienteId, tatuadorAutenticado.id());
    }

    @GetMapping("/api/v1/anamneses/{id}")
    public AnamneseResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping("/api/v1/anamneses")
    @ResponseStatus(HttpStatus.CREATED)
    public AnamneseResponse criar(@Valid @RequestBody AnamneseInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/api/v1/anamneses/{id}")
    public AnamneseResponse atualizar(@PathVariable Integer id, @Valid @RequestBody AnamneseInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/api/v1/anamneses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
