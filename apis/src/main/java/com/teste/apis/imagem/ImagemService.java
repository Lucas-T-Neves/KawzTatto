package com.teste.apis.imagem;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teste.apis.exception.RecursoNaoEncontradoException;
import com.teste.apis.imagem.dto.ImagemResponseDTO;
import com.teste.apis.imagem.dto.ImagemUrlDTO;
import com.teste.apis.imagem.dto.TransformacaoParams;
import com.teste.apis.integration.cloudinary.CloudinaryClient;
import com.teste.apis.integration.cloudinary.dto.CloudinaryUploadResult;
import com.teste.apis.integration.cloudinary.dto.TransformacaoUrl;

/** Fluxo de imagens: upload, listagem, detalhe, URL transformada e remocao. */
@Service
public class ImagemService {

    private static final Logger log = LoggerFactory.getLogger(ImagemService.class);
    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;

    private final ImagemRepository repository;
    private final CloudinaryClient cloudinaryClient;

    public ImagemService(ImagemRepository repository, CloudinaryClient cloudinaryClient) {
        this.repository = repository;
        this.cloudinaryClient = cloudinaryClient;
    }

    /** Valida o conteúdo, envia ao provedor e salva o registro. */
    public ImagemResponseDTO enviar(MultipartFile arquivo, String pasta) {
        validarArquivo(arquivo);

        byte[] dados;
        try {
            dados = arquivo.getBytes();
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Nao foi possivel ler o arquivo enviado");
        }

        // valida pelo conteúdo, não pela extensão
        TipoImagem.detectar(dados, arquivo.getOriginalFilename());

        log.debug("Enviando imagem para o Cloudinary ({} bytes)", dados.length);
        CloudinaryUploadResult resultado = cloudinaryClient.upload(dados, pasta);

        Imagem imagem = Imagem.de(
                resultado.publicId(),
                resultado.url(),
                resultado.formato(),
                resultado.bytes(),
                resultado.largura(),
                resultado.altura(),
                OffsetDateTime.now());

        return ImagemResponseDTO.de(repository.save(imagem));
    }

    public List<ImagemResponseDTO> listar() {
        return repository.findAllByOrderByCriadoEmDesc().stream()
                .map(ImagemResponseDTO::de)
                .toList();
    }

    public ImagemResponseDTO buscarPorId(Long id) {
        return ImagemResponseDTO.de(buscarEntidade(id));
    }

    /** Gera a URL transformada sem novo upload. */
    public ImagemUrlDTO gerarUrl(Long id, TransformacaoParams params) {
        Imagem imagem = buscarEntidade(id);

        TransformacaoUrl transformacao = new TransformacaoUrl(
                nuloSeVazio(params.largura()),
                nuloSeVazio(params.altura()),
                nuloSeVazio(params.corte()),
                nuloSeVazio(params.qualidade()),
                nuloSeVazio(params.formato()),
                nuloSeVazio(params.efeito()));

        String url = cloudinaryClient.gerarUrl(imagem.getPublicId(), transformacao);
        return new ImagemUrlDTO(url);
    }

    private String nuloSeVazio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /** Remove no provedor e no banco. */
    public void remover(Long id) {
        Imagem imagem = buscarEntidade(id);
        cloudinaryClient.remover(imagem.getPublicId());
        repository.delete(imagem);
    }

    Imagem buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Imagem nao encontrada: " + id));
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new com.teste.apis.exception.RegraNegocioException("Envie um arquivo de imagem");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new com.teste.apis.exception.RegraNegocioException("Arquivo maior que o limite permitido de 10 MB");
        }
    }
}
