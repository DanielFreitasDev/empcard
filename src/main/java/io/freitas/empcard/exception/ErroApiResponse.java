package io.freitas.empcard.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estrutura padronizada de erros retornados pela API.
 */
public record ErroApiResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
