package com.kawz.tattoo.api.movimentacao;

import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoInput;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResumoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public MovimentacaoController(MovimentacaoService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public Page<MovimentacaoResponse> listar(
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(tatuadorAutenticado.id(), mes, status, projetoId, clienteId,
                Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/resumo")
    public MovimentacaoResumoResponse resumo(@RequestParam String mes) {
        return service.resumo(mes, tatuadorAutenticado.id());
    }

    @GetMapping("/{id}")
    public MovimentacaoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse criar(@Valid @RequestBody MovimentacaoInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public MovimentacaoResponse atualizar(@PathVariable Integer id,
                                          @Valid @RequestBody MovimentacaoInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
