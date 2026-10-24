package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.ImagemInput;
import com.kawz.tattoo.api.dto.ApiModels.ImagemResponse;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Imagem;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.repository.ImagemRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class ImagemService {
    private final ImagemRepository imagens;
    private final ProjetoRepository projetos;

    public ImagemService(ImagemRepository imagens, ProjetoRepository projetos) {
        this.imagens = imagens;
        this.projetos = projetos;
    }

    @Transactional(readOnly = true)
    public List<ImagemResponse> listarPorProjeto(Integer projetoId) {
        return imagens.findByProjetoIdOrderByCriadoEmDesc(projetoId).stream()
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ImagemResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional
    public ImagemResponse criar(ImagemInput input) {
        if (imagens.existsByPublicId(input.publicId())) {
            throw new BusinessException("publicId de imagem já cadastrado");
        }
        return toResponse(imagens.save(new Imagem(obterProjeto(input.projetoId()), input.publicId(), input.url(),
                input.formato(), input.bytes(), input.larguraAltura())));
    }

    @Transactional
    public ImagemResponse atualizar(Integer id, ImagemInput input) {
        Imagem imagem = obter(id);
        if (imagens.existsByPublicIdAndIdNot(input.publicId(), id)) {
            throw new BusinessException("publicId de imagem já cadastrado");
        }
        imagem.vincularProjeto(obterProjeto(input.projetoId()));
        imagem.atualizar(input.publicId(), input.url(), input.formato(), input.bytes(), input.larguraAltura());
        return toResponse(imagem);
    }

    @Transactional
    public void excluir(Integer id) { imagens.delete(obter(id)); }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Imagem obter(Integer id) {
        return imagens.findById(id).orElseThrow(() -> new ResourceNotFoundException("Imagem", id));
    }
}
