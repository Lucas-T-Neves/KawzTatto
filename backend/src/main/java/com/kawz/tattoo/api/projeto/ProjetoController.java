package com.kawz.tattoo.api.projeto;

import com.kawz.tattoo.api.projeto.dto.ProjetoInput;
import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.api.projeto.dto.StatusRequest;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.projeto.ProjetoService;
import jakarta.validation.Valid;
import com.kawz.tattoo.api.web.PaginaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos")
public class ProjetoController {

    private final ProjetoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ProjetoController(ProjetoService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public PaginaResponse<ProjetoResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(service.listar(tatuadorAutenticado.id(), Paginacao.de(pagina, tamanho)));
    }

    @GetMapping("/{id}")
    public ProjetoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoResponse criar(@Valid @RequestBody ProjetoInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public ProjetoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ProjetoInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @PatchMapping("/{id}/status")
    public ProjetoResponse alterarStatus(@PathVariable Integer id, @Valid @RequestBody StatusRequest input) {
        return service.alterarStatus(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
