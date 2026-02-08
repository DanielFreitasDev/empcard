package io.freitas.empcard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Estrutura principal do relatorio analitico mensal de uma pessoa.
 */
public record RelatorioPessoaDto(
        Long pessoaId,
        String nomePessoa,
        String cpfMascarado,
        YearMonth competencia,
        List<ResumoCartaoRelatorioDto> cartoes,
        BigDecimal totalGeralDevido,
        BigDecimal totalGeralPago,
        BigDecimal totalGeralSaldo
) {
}
