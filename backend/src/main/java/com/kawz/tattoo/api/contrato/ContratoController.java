package com.kawz.tattoo.api.contrato;

import com.kawz.tattoo.api.contrato.dto.AssinaturaInput;
import com.kawz.tattoo.api.contrato.dto.ContratoCreate;
import com.kawz.tattoo.api.contrato.dto.ContratoInput;
import com.kawz.tattoo.api.contrato.dto.ContratoPdfInput;
import com.kawz.tattoo.api.contrato.dto.ContratoResponse;
import com.kawz.tattoo.api.contrato.dto.ContratoViewResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.contrato.ContratoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contratos")
public class ContratoController {

    private final ContratoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ContratoController(ContratoService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    /** Lista pela view vw_contratos (paginada), restrita ao tatuador autenticado. */
    @GetMapping
    public Page<ContratoViewResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(tatuadorAutenticado.id(), Paginacao.semOrdenacao(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ContratoViewResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse criar(@Valid @RequestBody ContratoCreate input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public ContratoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ContratoInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    /** Gera o PDF no APITemplate e grava pdfUrl/transactionRef. Corpo opcional. */
    @PostMapping("/{id}/pdf")
    public ContratoResponse gerarPdf(@PathVariable Integer id,
                                     @Valid @RequestBody(required = false) ContratoPdfInput input) {
        return service.gerarPdf(id, input, tatuadorAutenticado.id());
    }

    /** Registra a assinatura (data padrão: hoje quando ausente no corpo). */
    @PatchMapping("/{id}/assinatura")
    public ContratoResponse assinar(@PathVariable Integer id,
                                    @RequestBody(required = false) AssinaturaInput input) {
        return service.assinar(id, input == null ? new AssinaturaInput(null) : input,
                tatuadorAutenticado.id());
    }
}
