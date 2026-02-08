package io.freitas.empcard.exception;

/**
 * Excecao para violacoes de regra de negocio.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
