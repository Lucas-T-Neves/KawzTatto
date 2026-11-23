package com.kawz.tattoo.domain.imagem;

import com.kawz.tattoo.api.imagem.ImagemMapper;
import com.kawz.tattoo.api.imagem.dto.ImagemInput;
import com.kawz.tattoo.api.imagem.dto.ImagemResponse;
import com.kawz.tattoo.application.gateway.ArmazenamentoImagensGateway;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Imagens dos projetos do tatuador autenticado. O acesso ao armazenamento passa
 * por {@link ArmazenamentoImagensGateway}, não pelo cliente do fornecedor.
 */
@Service
public class ImagemService {

    private final ImagemRepository imagens;
    private final ProjetoRepository projetos;
    private final ArmazenamentoImagensGateway armazenamento;

    public ImagemService(ImagemRepository imagens, ProjetoRepository projetos,
                         ArmazenamentoImagensGateway armazenamento) {
        this.imagens = imagens;
        this.projetos = projetos;
        this.armazenamento = armazenamento;
    }

    /**
     * Envia o arquivo ao armazenamento e registra a imagem no projeto. Se o banco
     * falhar depois do upload, o arquivo é removido para não ficar órfão.
     */
    @Transactional
    public ImagemResponse enviar(Integer projetoId, MultipartFile arquivo, Integer tatuadorId) {
        Projeto projeto = obterProjetoDoTatuador(projetoId, tatuadorId);
        if (arquivo == null || arquivo.isEmpty()) throw new BusinessException("Arquivo de imagem vazio");
        String tipo = arquivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) throw new BusinessException("O arquivo precisa ser uma imagem");
        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler o arquivo enviado");
        }
        ArmazenamentoImagensGateway.ImagemEnviada enviada = armazenamento.enviar(conteudo, projetoId);
        try {
            return ImagemMapper.toResponse(imagens.save(new Imagem(projeto, enviada.publicId(), enviada.url(),
                    enviada.formato(), enviada.bytes() == null ? null : String.valueOf(enviada.bytes()),
                    enviada.largura())));
        } catch (RuntimeException ex) {
            armazenamento.remover(enviada.publicId());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ImagemResponse> listarPorProjeto(Integer projetoId, Integer tatuadorId) {
        obterProjetoDoTatuador(projetoId, tatuadorId);
        // stream().map().toList() converte cada imagem encontrada para seu
        // DTO e reúne o resultado sem alterar a lista original do repository.
        return imagens.findByProjetoIdAndProjeto_Tatuador_IdOrderByCriadoEmDesc(projetoId, tatuadorId).stream()
                .map(ImagemMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ImagemResponse buscar(Integer id, Integer tatuadorId) {
        return ImagemMapper.toResponse(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public ImagemResponse criar(ImagemInput input, Integer tatuadorId) {
        if (imagens.existsByPublicId(input.publicId())) {
            throw new BusinessException("publicId de imagem já cadastrado");
        }
        return ImagemMapper.toResponse(imagens.save(new Imagem(obterProjetoDoTatuador(input.projetoId(), tatuadorId),
                input.publicId(), input.url(), input.formato(), input.bytes(), input.larguraAltura())));
    }

    @Transactional
    public ImagemResponse atualizar(Integer id, ImagemInput input, Integer tatuadorId) {
        Imagem imagem = obterDoTatuador(id, tatuadorId);
        if (imagens.existsByPublicIdAndIdNot(input.publicId(), id)) {
            throw new BusinessException("publicId de imagem já cadastrado");
        }
        imagem.vincularProjeto(obterProjetoDoTatuador(input.projetoId(), tatuadorId));
        imagem.atualizar(input.publicId(), input.url(), input.formato(), input.bytes(), input.larguraAltura());
        return ImagemMapper.toResponse(imagem);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        Imagem imagem = obterDoTatuador(id, tatuadorId);
        String publicId = imagem.getPublicId();
        imagens.delete(imagem);
        imagens.flush();
        armazenamento.remover(publicId);
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Imagem obterDoTatuador(Integer id, Integer tatuadorId) {
        return imagens.findByIdAndProjeto_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem", id));
    }
}
