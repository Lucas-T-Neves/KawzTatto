package com.teste.apis.integration.apoio;

/**
 * Erro cru devolvido por um provedor externo, capturado durante a chamada HTTP.
 *
 * <p>Fica dentro da camada de integracao e NUNCA vaza para fora: cada client
 * traduz para {@link com.teste.apis.exception.IntegrationException}, que e o
 * que o handler transforma em resposta HTTP.</p>
 */
public class ErroProvedor extends RuntimeException {

    private final int status;
    private final String corpo;
    private final Long retryAfter;

    public ErroProvedor(int status, String corpo, Long retryAfter) {
        super("erro " + status);
        this.status = status;
        this.corpo = corpo;
        this.retryAfter = retryAfter;
    }

    /** Status HTTP devolvido pelo provedor (ex.: 429, 401, 404). */
    public int getStatus() {
        return status;
    }

    /** Corpo da resposta de erro, como texto. */
    public String getCorpo() {
        return corpo;
    }

    /** Valor do cabecalho Retry-After, quando o provedor o envia (usado no 429). */
    public Long getRetryAfter() {
        return retryAfter;
    }
}
