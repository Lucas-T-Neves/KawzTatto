package com.teste.apis.imagem;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.teste.apis.imagem.dto.ImagemResponseDTO;
import com.teste.apis.imagem.dto.ImagemUrlDTO;
import com.teste.apis.imagem.dto.TransformacaoParams;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/imagens")
public class ImagemController {

    private final ImagemService service;

    public ImagemController(ImagemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ImagemResponseDTO> enviar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "pasta", required = false) String pasta,
            UriComponentsBuilder uriBuilder) {

        ImagemResponseDTO criada = service.enviar(arquivo, pasta);
        URI location = uriBuilder.path("/imagens/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public List<ImagemResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ImagemResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/url")
    public ImagemUrlDTO gerarUrl(@PathVariable Long id,
                                 @Valid @ModelAttribute TransformacaoParams params) {
        return service.gerarUrl(id, params);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
