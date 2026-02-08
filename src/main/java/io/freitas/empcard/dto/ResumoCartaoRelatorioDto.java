package io.freitas.empcard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consolidacao mensal por cartao para uma pessoa.
 */
public record ResumoCartaoRelatorioDto(
        Long cartaoId,
        String numeroMascarado,
        String bandeira,
        String banco,
        LocalDate dataVencimento,
        BigDecimal saldoAnterior,
        BigDecimal totalAvulso,
        BigDecimal totalParcelado,
        BigDecimal totalFixo,
        BigDecimal totalCompras,
        BigDecimal jurosMulta,
        BigDecimal totalPagamentos,
        BigDecimal totalDevido,
        BigDecimal saldoFinal,
        List<ItemRelatorioDto> itens
) {
}
