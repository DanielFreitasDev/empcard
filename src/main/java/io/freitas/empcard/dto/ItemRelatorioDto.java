package io.freitas.empcard.dto;

import io.freitas.empcard.model.TipoLancamento;

import java.math.BigDecimal;

/**
 * Item detalhado de cobranca em um relatorio mensal.
 */
public record ItemRelatorioDto(
        String descricao,
        TipoLancamento tipoLancamento,
        String parcela,
        BigDecimal valor,
        String observacao
) {
}
