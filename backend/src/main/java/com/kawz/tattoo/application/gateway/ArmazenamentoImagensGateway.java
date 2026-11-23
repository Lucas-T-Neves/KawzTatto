package com.kawz.tattoo.application.gateway;

/**
 * Porta de armazenamento de imagens. A aplicação depende desta capacidade, não
 * do fornecedor: o Cloudinary é apenas a implementação atual
 * ({@code infrastructure.integration.CloudinaryArmazenamentoImagensGateway}).
 *
 * <p>Política de falha: o upload bloqueia a operação (a imagem não é registrada
 * no banco se o envio falhar) e a remoção é "melhor esforço" (o registro já foi
 * apagado, a falha é apenas registrada em log).</p>
 */
public interface ArmazenamentoImagensGateway {

    /** O que o sistema guarda de uma imagem enviada. */
    record ImagemEnviada(String publicId, String url, String formato, Long bytes, Integer largura, Integer altura) {}

    /**
     * Envia a imagem do projeto. Lança {@code IntegracaoException} (HTTP 502)
     * quando o serviço não está configurado ou falha.
     */
    ImagemEnviada enviar(byte[] conteudo, Integer projetoId);

    /** Remove a imagem do armazenamento. Falhas são registradas, não propagadas. */
    void remover(String publicId);
}
