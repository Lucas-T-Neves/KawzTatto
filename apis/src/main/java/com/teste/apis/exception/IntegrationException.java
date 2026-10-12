package com.teste.apis.exception;

/**
 * Erro de integracao com um provedor externo (APITemplate.io, Cloudinary ou Nylas).
 * Carrega o provedor, o status devolvido por ele e os dados necessarios para
 * o handler traduzir em HTTP adequado (502, 503 ou 504).
 */
public class IntegrationException extends RuntimeException {

    private final String provedor;
    private final Integer statusProvedor;
    private final boolean timeout;
    private final Long retryAfterSeconds;

    public IntegrationException(String provedor, String mensagem, Integer statusProvedor,
                                boolean timeout, Long retryAfterSeconds) {
        super(mensagem);
        this.provedor = provedor;
        this.statusProvedor = statusProvedor;
        this.timeout = timeout;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** Falha comum do provedor: vira 502. */
    public static IntegrationException falhaProvedor(String provedor, String mensagem, Integer statusProvedor) {
        return new IntegrationException(provedor, mensagem, statusProvedor, false, null);
    }

    /** Timeout na chamada do provedor: vira 504. */
    public static IntegrationException timeout(String provedor, String mensagem) {
        return new IntegrationException(provedor, mensagem, null, true, null);
    }

    /** Limite de requisicoes excedido (429 do provedor): vira 503. */
    public static IntegrationException muitosRequests(String provedor, String mensagem, Long retryAfterSeconds) {
        return new IntegrationException(provedor, mensagem, 429, false, retryAfterSeconds);
    }

    /** Chave/credencial invalida (401 do provedor): erro de configuracao do servidor, vira 502. */
    public static IntegrationException credencialInvalida(String provedor) {
        return new IntegrationException(provedor, "Falha de autenticacao com o provedor", 401, false, null);
    }

    public String getProvedor() {
        return provedor;
    }

    public Integer getStatusProvedor() {
        return statusProvedor;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
