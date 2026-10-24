package com.kawz.tattoo.api.exception;

import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Erros da API no padrão RFC 9457 (Problem Details), nativo do Spring MVC:
 * type, title, status, detail, instance + propriedade extra "fields"
 * (erros de validação campo a campo) e "timestamp".
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> naoEncontrado(ResourceNotFoundException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado", "nao-encontrado",
                ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> regraViolada(BusinessException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", "regra-violada",
                ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> conflito(DataIntegrityViolationException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "Conflito de integridade", "conflito",
                "A operação viola uma restrição de integridade ou o registro está em uso",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> erroInesperado(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", "erro-interno",
                "Ocorreu um erro interno inesperado", request.getRequestURI(), null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        ProblemDetail pd = criar(HttpStatus.BAD_REQUEST, "Dados inválidos", "dados-invalidos",
                "Um ou mais campos são inválidos", uri(request));
        pd.setProperty("fields", campos);
        return ResponseEntity.status(status).body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = criar(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", "corpo-invalido",
                "JSON inválido ou com valor em formato incorreto", uri(request));
        return ResponseEntity.status(status).body(pd);
    }

    private String uri(WebRequest request) {
        return request instanceof ServletWebRequest servlet
                ? servlet.getRequest().getRequestURI()
                : request.getDescription(false);
    }

    private ResponseEntity<ProblemDetail> responder(HttpStatus status, String titulo, String tipo,
            String detail, String instance, Map<String, String> campos) {
        ProblemDetail pd = criar(status, titulo, tipo, detail, instance);
        if (campos != null) pd.setProperty("fields", campos);
        return ResponseEntity.status(status).body(pd);
    }

    private ProblemDetail criar(HttpStatus status, String titulo, String tipo, String detail, String instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(titulo);
        pd.setType(URI.create("https://kawz-tattoo-api.local/erros/" + tipo));
        if (instance != null && !instance.isBlank()) pd.setInstance(URI.create(instance));
        pd.setProperty("timestamp", LocalDateTime.now());
        return pd;
    }
}
