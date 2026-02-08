package io.freitas.empcard.exception;

/**
 * Excecao para sinalizar ausencia de um recurso solicitado.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
