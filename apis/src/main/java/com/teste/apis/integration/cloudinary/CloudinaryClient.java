package com.teste.apis.integration.cloudinary;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.integration.cloudinary.dto.CloudinaryUploadResult;
import com.teste.apis.integration.cloudinary.dto.TransformacaoUrl;

/**
 * Client do Cloudinary. Encapsula o SDK para que o service e os testes
 * nao dependam dele.
 */
@Component
public class CloudinaryClient {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryClient.class);
    private static final String PROVEDOR = "Cloudinary";

    private final Cloudinary cloudinary;
    private final CloudinaryProperties props;

    public CloudinaryClient(Cloudinary cloudinary, CloudinaryProperties props) {
        this.cloudinary = cloudinary;
        this.props = props;
    }

    /** Sobe a imagem uma vez e devolve os dados relevantes. */
    public CloudinaryUploadResult upload(byte[] dados, String pasta) {
        try {
            Map<String, Object> opcoes = ObjectUtils.asMap(
                    "folder", pasta == null || pasta.isBlank() ? props.getPastaPadrao() : pasta,
                    "resource_type", "image");

            Map<?, ?> resultado = cloudinary.uploader().upload(dados, opcoes);

            return new CloudinaryUploadResult(
                    texto(resultado.get("public_id")),
                    texto(resultado.get("secure_url")),
                    texto(resultado.get("format")),
                    numero(resultado.get("bytes")),
                    inteiro(resultado.get("width")),
                    inteiro(resultado.get("height")));
        } catch (IOException e) {
            log.error("Falha de IO ao enviar imagem para o provedor={}", PROVEDOR);
            throw IntegrationException.falhaProvedor(PROVEDOR, "Falha ao enviar a imagem", null);
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao enviar imagem para o provedor={}", PROVEDOR, e);
            throw IntegrationException.falhaProvedor(PROVEDOR, "Falha ao enviar a imagem", null);
        }
    }

    /** Remove a imagem no provedor. */
    public void remover(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Falha de IO ao remover imagem do provedor={} publicId={}", PROVEDOR, publicId);
            throw IntegrationException.falhaProvedor(PROVEDOR, "Falha ao remover a imagem", null);
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao remover imagem do provedor={} publicId={}", PROVEDOR, publicId, e);
            throw IntegrationException.falhaProvedor(PROVEDOR, "Falha ao remover a imagem", null);
        }
    }

    /** Gera a URL transformada sem reenviar o arquivo: a edicao e feita pela propria URL. */
    public String gerarUrl(String publicId, TransformacaoUrl transformacao) {
        Transformation t = new Transformation();

        if (temValor(transformacao.largura())) {
            t.width(valor(transformacao.largura()));
        }
        if (temValor(transformacao.altura())) {
            t.height(valor(transformacao.altura()));
        }
        if (temValor(transformacao.corte())) {
            t.crop(transformacao.corte());
        }
        if (temValor(transformacao.qualidade())) {
            t.quality(valor(transformacao.qualidade()));
        }
        if (temValor(transformacao.formato())) {
            t.fetchFormat(transformacao.formato());
        }
        if (temValor(transformacao.efeito())) {
            t.effect(transformacao.efeito());
        }

        return cloudinary.url().transformation(t).generate(publicId);
    }

    private boolean temValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    /** 'auto' continua texto; numeros viram Integer para o SDK. */
    private Object valor(String v) {
        if ("auto".equalsIgnoreCase(v)) {
            return "auto";
        }
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException e) {
            return v;
        }
    }

    private String texto(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }

    private Long numero(Object valor) {
        return valor == null ? null : ((Number) valor).longValue();
    }

    private Integer inteiro(Object valor) {
        return valor == null ? null : ((Number) valor).intValue();
    }
}
