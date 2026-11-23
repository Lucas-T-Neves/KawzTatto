package com.kawz.tattoo.infrastructure.integration;

import com.kawz.tattoo.application.gateway.ArmazenamentoImagensGateway;
import org.springframework.stereotype.Component;

/**
 * Adaptador da porta de armazenamento de imagens sobre o SDK do Cloudinary.
 * Mantém a aplicação livre do fornecedor: trocar de storage significa criar
 * outro adaptador, sem tocar em {@code ImagemService} ou {@code ProjetoService}.
 */
@Component
public class CloudinaryArmazenamentoImagensGateway implements ArmazenamentoImagensGateway {

    private final CloudinaryClient cloudinary;

    public CloudinaryArmazenamentoImagensGateway(CloudinaryClient cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public ImagemEnviada enviar(byte[] conteudo, Integer projetoId) {
        CloudinaryClient.Enviada enviada = cloudinary.enviar(conteudo, projetoId);
        return new ImagemEnviada(enviada.publicId(), enviada.url(), enviada.formato(),
                enviada.bytes(), enviada.largura(), enviada.altura());
    }

    @Override
    public void remover(String publicId) {
        cloudinary.remover(publicId);
    }
}
