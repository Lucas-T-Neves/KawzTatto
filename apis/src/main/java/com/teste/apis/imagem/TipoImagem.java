package com.teste.apis.imagem;

/**
 * Valida o tipo da imagem pelo CONTEUDO (magic bytes), nao pela extensao.
 * Aceita apenas JPEG, PNG e WebP. Arquivo invalido e erro de requisicao (400),
 * por isso lanca RegraNegocioException em vez de erro de integracao.
 */
public final class TipoImagem {

    public static final String JPEG = "jpeg";
    public static final String PNG = "png";
    public static final String WEBP = "webp";

    private TipoImagem() {
    }

    /** Devolve o formato detectado ou lanca RegraNegocioException (400). */
    public static String detectar(byte[] dados, String nomeArquivo) {
        if (dados == null || dados.length < 12) {
            throw new com.teste.apis.exception.RegraNegocioException(
                    "Arquivo muito pequeno ou vazio: nao foi possivel identificar o formato");
        }

        if (ehJpeg(dados)) {
            return JPEG;
        }
        if (ehPng(dados)) {
            return PNG;
        }
        if (ehWebp(dados)) {
            return WEBP;
        }

        throw new com.teste.apis.exception.RegraNegocioException(
                "Formato nao suportado (" + nomeArquivo + "): use JPEG, PNG ou WebP");
    }

    private static boolean ehJpeg(byte[] d) {
        return (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF;
    }

    private static boolean ehPng(byte[] d) {
        return (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G'
                && (d[4] & 0xFF) == 0x0D && (d[5] & 0xFF) == 0x0A
                && (d[6] & 0xFF) == 0x1A && (d[7] & 0xFF) == 0x0A;
    }

    private static boolean ehWebp(byte[] d) {
        return d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P';
    }
}
