package com.teste.apis.exception;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduz excecoes em respostas HTTP no formato ProblemDetail (RFC 7807).
 * Nunca repassa status nem corpo cru do provedor para o cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- Validacao (400) ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException ex) {
        List<Map<String, String>> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("campo", erro.getField());
                    item.put("mensagem", erro.getDefaultMessage());
                    return item;
                })
                .collect(Collectors.toList());

        ProblemDetail detail = base(HttpStatus.BAD_REQUEST, "Um ou mais campos da requisicao sao invalidos");
        detail.setProperty("errors", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    // ---------- Requisicao malformada (400) ----------

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class,
            MultipartException.class,
            MaxUploadSizeExceededException.class,
            IllegalArgumentException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ResponseEntity<ProblemDetail> tratarRequisicaoInvalida(Exception ex) {
        log.warn("Requisição inválida [{}]: {}", ex.getClass().getName(), ex.getMessage());
        String mensagem = ex instanceof MaxUploadSizeExceededException
                ? "Arquivo maior que o limite permitido de 10 MB"
                : "Requisicao invalida";
        ProblemDetail detail = base(HttpStatus.BAD_REQUEST, mensagem);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    // ---------- Regra de negocio (400) ----------
    // Erro esperado do dominio: a mensagem e escrita para o cliente e pode ser exibida.

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ProblemDetail> tratarRegraNegocio(RegraNegocioException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        ProblemDetail detail = base(HttpStatus.BAD_REQUEST,
                ex.getMessage() == null ? "Requisicao invalida" : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    // ---------- Nao encontrado (404) ----------

    @ExceptionHandler({RecursoNaoEncontradoException.class,
            NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(Exception ex) {
        ProblemDetail detail = base(HttpStatus.NOT_FOUND, ex.getMessage() == null
                ? "Recurso nao encontrado"
                : ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
    }

    // ---------- Integracao (502 / 504 / 503) ----------

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ProblemDetail> tratarIntegracao(IntegrationException ex) {
        if (ex.isTimeout()) {
            log.error("Timeout ao chamar provedor={} mensagem={}", ex.getProvedor(), ex.getMessage());
            return responder(HttpStatus.GATEWAY_TIMEOUT,
                    "O servico externo nao respondeu a tempo",
                    ex, null, null);
        }

        if (Integer.valueOf(429).equals(ex.getStatusProvedor())) {
            log.warn("Limite de requisicoes excedido no provedor={}", ex.getProvedor());
            return responder(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servico externo temporariamente indisponivel",
                    ex, null, ex.getRetryAfterSeconds());
        }

        log.error("Falha de integracao provedor={} statusProvedor={} mensagem={}",
                ex.getProvedor(), ex.getStatusProvedor(), ex.getMessage());

        Integer statusProvedor = ex.getStatusProvedor();
        if (Integer.valueOf(401).equals(statusProvedor)) {
            log.error("Credencial invalida no provedor={}: revise a configuracao do servidor", ex.getProvedor());
        }

        return responder(HttpStatus.BAD_GATEWAY, "Falha ao consultar o servico externo",
                ex, statusProvedor, null);
    }

    // ---------- Nao esperado (500) ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarNaoEsperado(Exception ex) {
        log.error("Erro inesperado nao tratado: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        ProblemDetail detail = base(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
    }

    // ---------- utilitarios ----------

    private ResponseEntity<ProblemDetail> responder(HttpStatus status, String titulo,
                                                    IntegrationException ex,
                                                    Integer statusProvedor,
                                                    Long retryAfterSeconds) {
        ProblemDetail detail = base(status, titulo);
        detail.setProperty("provedor", ex.getProvedor());
        if (statusProvedor != null) {
            detail.setProperty("statusProvedor", statusProvedor);
        }
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (retryAfterSeconds != null) {
            builder.header("Retry-After", String.valueOf(retryAfterSeconds));
        }
        return builder.body(detail);
    }

    private ProblemDetail base(HttpStatus status, String titulo) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(status.value()), titulo);
        detail.setTitle(titulo);
        detail.setType(URI.create("about:blank"));
        detail.setProperty("timestamp", OffsetDateTime.now().toString());
        return detail;
    }
}
