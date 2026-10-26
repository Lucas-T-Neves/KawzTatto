package com.kawz.tattoo.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kawz.tattoo.domain.exception.IntegracaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Upload e remoção de imagens no Cloudinary (SDK oficial). Sem chaves configuradas, o upload falha com 502. */
@Component
public class CloudinaryClient {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryClient.class);

    /** O que o sistema guarda de uma imagem enviada. */
    public record Enviada(String publicId, String url, String formato, Long bytes, Integer largura, Integer altura) {}

    private final Cloudinary cloudinary;
    private final boolean configurado;
    private final String pasta;

    public CloudinaryClient(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${cloudinary.pasta:kawz-tattoo}") String pasta) {
        this.configurado = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
        this.cloudinary = configurado
                ? new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret, "secure", true))
                : null;
        this.pasta = pasta;
    }

    public boolean configurado() { return configurado; }

    public Enviada enviar(byte[] conteudo, Integer projetoId) {
        if (!configurado) throw new IntegracaoException("Cloudinary não configurado (defina CLOUDINARY_*)");
        try {
            Map<?, ?> r = cloudinary.uploader().upload(conteudo, ObjectUtils.asMap(
                    "folder", pasta + "/projeto-" + projetoId,
                    "resource_type", "image"));
            return new Enviada(
                    String.valueOf(r.get("public_id")),
                    String.valueOf(r.get("secure_url")),
                    r.get("format") == null ? null : String.valueOf(r.get("format")),
                    r.get("bytes") instanceof Number n ? n.longValue() : null,
                    r.get("width") instanceof Number n ? n.intValue() : null,
                    r.get("height") instanceof Number n ? n.intValue() : null);
        } catch (Exception ex) {
            throw new IntegracaoException("Falha ao enviar a imagem ao Cloudinary", ex);
        }
    }

    /** Remoção "melhor esforço": o registro no banco já foi apagado, então só registramos o erro. */
    public void remover(String publicId) {
        if (!configurado || publicId == null) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception ex) {
            log.warn("Não foi possível remover {} do Cloudinary: {}", publicId, ex.getMessage());
        }
    }
}
