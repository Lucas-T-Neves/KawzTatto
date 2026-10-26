package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.AssinaturaInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoCreate;
import com.kawz.tattoo.api.dto.ApiModels.ContratoInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoPdfInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoResponse;
import com.kawz.tattoo.api.dto.ApiModels.ContratoViewResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.ContratoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contratos")
public class ContratoController {
    private final ContratoService service;

    public ContratoController(ContratoService service) { this.service = service; }

    /** Lista pela view vw_contratos (paginada). */
    @GetMapping
    public Page<ContratoViewResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(Paginacao.semOrdenacao(pagina, tamanho));
    }

    /** Contrato pela view vw_contratos. */
    @GetMapping("/{id}")
    public ContratoViewResponse buscar(@PathVariable Integer id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse criar(@Valid @RequestBody ContratoCreate input) {
        return service.criar(input);
    }

    /** Atualiza os termos do contrato (entidade). */
    @PutMapping("/{id}")
    public ContratoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ContratoInput input) {
        return service.atualizar(id, input);
    }

    /** Gera o PDF no APITemplate e grava pdfUrl/transactionRef no contrato. Corpo opcional (cpfCliente, tamanhoEstimado). */
    @PostMapping("/{id}/pdf")
    public ContratoResponse gerarPdf(@PathVariable Integer id,
                                     @Valid @RequestBody(required = false) ContratoPdfInput input) {
        return service.gerarPdf(id, input);
    }

    /** Registra a assinatura (data padrão: hoje quando ausente no corpo). */
    @PatchMapping("/{id}/assinatura")
    public ContratoResponse assinar(@PathVariable Integer id,
                                    @RequestBody(required = false) AssinaturaInput input) {
        return service.assinar(id, input == null ? new AssinaturaInput(null) : input);
    }
}
