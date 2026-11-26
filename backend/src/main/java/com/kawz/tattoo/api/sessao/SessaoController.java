package com.kawz.tattoo.api.sessao;

import com.kawz.tattoo.api.sessao.dto.SessaoInput;
import com.kawz.tattoo.api.sessao.dto.SessaoRemarcarInput;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.sessao.SessaoService;
import jakarta.validation.Valid;
import com.kawz.tattoo.api.web.PaginaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sessoes")
public class SessaoController {

    private final SessaoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public SessaoController(SessaoService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public PaginaResponse<SessaoResponse> listar(
            @RequestParam(required = false) Integer projetoId,
            @RequestParam(required = false) LocalDate dataApos,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        Pageable pageable = Paginacao.de(pagina, tamanho);
        Integer tatuadorId = tatuadorAutenticado.id();
        if (dataApos != null) return PaginaResponse.de(service.listarPorDataApos(dataApos.atStartOfDay(), tatuadorId, pageable));
        return projetoId == null
                ? PaginaResponse.de(service.listar(tatuadorId, pageable))
                : PaginaResponse.de(service.listarPorProjeto(projetoId, tatuadorId, pageable));
    }

    @GetMapping("/calendario")
    public PaginaResponse<SessaoResponse> calendario(@RequestParam LocalDate inicio,
                                           @RequestParam LocalDate fim,
                                           @RequestParam(defaultValue = "0") int pagina,
                                           @RequestParam(defaultValue = "100") int tamanho) {
        if (fim.isBefore(inicio)) throw new BusinessException("fim não pode ser anterior a inicio");
        return PaginaResponse.de(service.listarPorPeriodo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay(),
                tatuadorAutenticado.id(), Paginacao.de(pagina, tamanho)));
    }

    @GetMapping("/{id}")
    public SessaoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse criar(@Valid @RequestBody SessaoInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public SessaoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody SessaoInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @PatchMapping("/{id}/remarcar")
    public SessaoResponse remarcar(@PathVariable Integer id,
                                   @Valid @RequestBody SessaoRemarcarInput input) {
        return service.remarcar(id, input.dataHorario(), tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
