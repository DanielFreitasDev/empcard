package io.freitas.empcard.dto;

import io.freitas.empcard.model.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Saida padronizada de lancamento na API REST.
 */
public record LancamentoResponseDto(
        Long id,
        Long pessoaId,
        String nomePessoa,
        Long cartaoId,
        String numeroCartao,
        String descricao,
        TipoLancamento tipo,
        BigDecimal valorTotal,
        Integer quantidadeParcelas,
        LocalDate dataCompra,
        LocalDate dataFimFixo,
        String observacao,
        boolean ativo
) {
}
