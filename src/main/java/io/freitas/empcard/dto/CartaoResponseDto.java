package io.freitas.empcard.dto;

/**
 * Saida padronizada de cartao na API REST.
 */
public record CartaoResponseDto(
        Long id,
        String numero,
        String bandeira,
        String banco,
        Integer diaFechamento,
        Integer diaVencimento,
        boolean ativo
) {
}
