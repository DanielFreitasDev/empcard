package io.freitas.empcard.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Centraliza tratamento de excecoes para os endpoints da API REST.
 */
@Slf4j
@RestControllerAdvice(basePackages = "io.freitas.empcard.controller.api")
public class TratadorGlobalApiException {

    /**
     * Trata erros de validacao de entrada retornando mensagem amigavel por campo invalido.
     *
     * @param ex      excecao de validacao do Spring
     * @param request requisicao atual para capturar o caminho invocado
     * @return resposta padronizada com status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApiResponse> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .toList();

        log.warn("Falha de validacao na API: path={}, detalhes={}", request.getRequestURI(), detalhes);

        ErroApiResponse erro = new ErroApiResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Dados de entrada invalidos",
                request.getRequestURI(),
                detalhes
        );
        return ResponseEntity.badRequest().body(erro);
    }

    /**
     * Trata excecao de recurso nao encontrado e devolve status 404.
     *
     * @param ex      excecao de dominio
     * @param request requisicao atual
     * @return erro padronizado de nao encontrado
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroApiResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso nao encontrado na API: path={}, mensagem={}", request.getRequestURI(), ex.getMessage());

        ErroApiResponse erro = new ErroApiResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    /**
     * Trata excecao de regra de negocio e devolve status 409.
     *
     * @param ex      excecao de regra
     * @param request requisicao atual
     * @return erro padronizado de conflito
     */
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroApiResponse> tratarRegra(RegraDeNegocioException ex, HttpServletRequest request) {
        log.warn("Regra de negocio violada na API: path={}, mensagem={}", request.getRequestURI(), ex.getMessage());

        ErroApiResponse erro = new ErroApiResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    /**
     * Trata qualquer erro inesperado evitando vazamento de detalhes sensiveis.
     *
     * @param ex      excecao nao tratada
     * @param request requisicao atual
     * @return erro padronizado com status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApiResponse> tratarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado na API: path={}", request.getRequestURI(), ex);

        ErroApiResponse erro = new ErroApiResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Erro interno inesperado",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    /**
     * Converte erro de campo em frase unica para facilitar leitura no cliente.
     *
     * @param erroCampo erro de um campo validado
     * @return texto no formato "campo: mensagem"
     */
    private String formatarErroCampo(FieldError erroCampo) {
        return erroCampo.getField() + ": " + erroCampo.getDefaultMessage();
    }
}
