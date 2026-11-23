package com.kawz.tattoo.api.imagem;

import com.kawz.tattoo.api.imagem.dto.ImagemInput;
import com.kawz.tattoo.api.imagem.dto.ImagemResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.imagem.ImagemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ImagemController {

    private final ImagemService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ImagemController(ImagemService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping("/api/v1/projetos/{projetoId}/imagens")
    public List<ImagemResponse> listarPorProjeto(@PathVariable Integer projetoId) {
        return service.listarPorProjeto(projetoId, tatuadorAutenticado.id());
    }

    /** Upload multipart (campo "arquivo") para o Cloudinary; registra a imagem no projeto. */
    @PostMapping(path = "/api/v1/projetos/{projetoId}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemResponse enviar(@PathVariable Integer projetoId,
                                 @RequestParam("arquivo") MultipartFile arquivo) {
        return service.enviar(projetoId, arquivo, tatuadorAutenticado.id());
    }

    @GetMapping("/api/v1/imagens/{id}")
    public ImagemResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping("/api/v1/imagens")
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemResponse criar(@Valid @RequestBody ImagemInput input) {
        return service.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/api/v1/imagens/{id}")
    public ImagemResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ImagemInput input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/api/v1/imagens/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }
}
