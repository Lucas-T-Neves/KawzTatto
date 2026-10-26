package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.ImagemInput;
import com.kawz.tattoo.api.dto.ApiModels.ImagemResponse;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Imagem;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.repository.ImagemRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import com.kawz.tattoo.integration.CloudinaryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class ImagemService {
    private final ImagemRepository imagens;
    private final ProjetoRepository projetos;
    private final CloudinaryClient cloudinary;

    public ImagemService(ImagemRepository imagens, ProjetoRepository projetos, CloudinaryClient cloudinary) {
        this.imagens = imagens;
        this.projetos = projetos;
        this.cloudinary = cloudinary;
    }

    /**
     * Envia o arquivo ao Cloudinary e registra a imagem no projeto. Se o banco falhar depois do upload,
     * o arquivo é removido do Cloudinary para não ficar órfão.
     */
    @Transactional
    public ImagemResponse enviar(Integer projetoId, MultipartFile arquivo) {
        Projeto projeto = obterProjeto(projetoId);
        if (arquivo == null || arquivo.isEmpty()) throw new BusinessException("Arquivo de imagem vazio");
        String tipo = arquivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) throw new BusinessException("O arquivo precisa ser uma imagem");
        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler o arquivo enviado");
        }
        CloudinaryClient.Enviada enviada = cloudinary.enviar(conteudo, projetoId);
        try {
            return toResponse(imagens.save(new Imagem(projeto, enviada.publicId(), enviada.url(), enviada.formato(),
                    enviada.bytes() == null ? null : String.valueOf(enviada.bytes()), enviada.largura())));
        } catch (RuntimeException ex) {
            cloudinary.remover(enviada.publicId());
            throw ex;
        }
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
    public void excluir(Integer id) {
        Imagem imagem = obter(id);
        String publicId = imagem.getPublicId();
        imagens.delete(imagem);
        imagens.flush();
        cloudinary.remover(publicId);
    }

    private Projeto obterProjeto(Integer id) {
        return projetos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Imagem obter(Integer id) {
        return imagens.findById(id).orElseThrow(() -> new ResourceNotFoundException("Imagem", id));
    }
}
