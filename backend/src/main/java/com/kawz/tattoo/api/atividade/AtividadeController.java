package com.kawz.tattoo.api.atividade;

import com.kawz.tattoo.api.atividade.dto.AtividadeInput;
import com.kawz.tattoo.api.atividade.dto.AtividadeResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.atividade.AtividadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/atividades")
public class AtividadeController {

    private final AtividadeService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public AtividadeController(AtividadeService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping("/{id}")
    public AtividadeResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtividadeResponse criar(@Valid @RequestBody AtividadeInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public AtividadeResponse atualizar(@PathVariable Integer id, @Valid @RequestBody AtividadeInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
