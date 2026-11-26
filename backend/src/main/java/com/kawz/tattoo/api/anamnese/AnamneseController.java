package com.kawz.tattoo.api.anamnese;

import com.kawz.tattoo.api.anamnese.dto.AnamneseInput;
import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.anamnese.AnamneseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/api/v1/anamneses/{id}/pdf")
    public AnamneseResponse gerarPdf(@PathVariable Integer id) {
        return service.gerarPdf(id, tatuadorAutenticado.id());
    }

    @PostMapping(path = "/api/v1/anamneses/{id}/documento-assinado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnamneseResponse enviarDocumentoAssinado(@PathVariable Integer id,
                                                     @RequestParam("arquivo") MultipartFile arquivo) {
        return service.enviarDocumentoAssinado(id, arquivo, tatuadorAutenticado.id());
    }

    @DeleteMapping("/api/v1/anamneses/{id}/documento-assinado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirDocumentoAssinado(@PathVariable Integer id) {
        service.excluirDocumentoAssinado(id, tatuadorAutenticado.id());
    }

    @DeleteMapping("/api/v1/anamneses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }

}
