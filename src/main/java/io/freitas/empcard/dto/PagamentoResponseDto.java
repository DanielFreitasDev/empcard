package io.freitas.empcard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Saida padronizada de pagamento na API REST.
 */
public record PagamentoResponseDto(
        Long id,
        Long pessoaId,
        String nomePessoa,
        Long cartaoId,
        String numeroCartao,
        LocalDate dataPagamento,
        BigDecimal valor,
        String observacao
) {
}
