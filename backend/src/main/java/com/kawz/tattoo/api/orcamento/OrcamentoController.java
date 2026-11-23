package com.kawz.tattoo.api.orcamento;

import com.kawz.tattoo.api.orcamento.dto.OrcamentoInput;
import com.kawz.tattoo.api.orcamento.dto.OrcamentoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.orcamento.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orcamentos")
public class OrcamentoController {

    private final OrcamentoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public OrcamentoController(OrcamentoService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public Page<OrcamentoResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(tatuadorAutenticado.id(), status, projetoId, clienteId,
                Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public OrcamentoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoResponse criar(@Valid @RequestBody OrcamentoInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public OrcamentoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody OrcamentoInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
