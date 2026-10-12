package com.teste.apis.exception;

/**
 * Erro de REGRA DE NEGOCIO: o pedido e sintaticamente valido, mas viola uma
 * regra do dominio (periodo inconsistente, fuso invalido, arquivo que nao e
 * imagem, Alem do tamanho permitido...).
 *
 * <p>Por que ter excecao propria em vez de {@link IllegalArgumentException}?</p>
 * <ul>
 *   <li><b>Regra de negocio</b> (esta classe): o erro esperado e mensagem
 *       clara Para o cliente — o handler devolve a propria mensagem com 400.</li>
 *   <li><b>Erro tecnico generico</b> (excecoes padrao do Java): falha de
 *       infraestrutura ou de codigo — o handler esconde o detalhe e devolve
 *       uma mensagem generica, logando o que aconteceu.</li>
 * </ul>
 *
 * <p>Assim o codigo diz a intencao: quem le um
 * {@code throw new RegraNegocioException(...)} sabe que e uma recusa do
 * dominio, nao um defeito tecnico.</p>
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
