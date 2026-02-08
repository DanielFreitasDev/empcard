package io.freitas.empcard.service;

import io.freitas.empcard.dto.ItemRelatorioDto;
import io.freitas.empcard.dto.RelatorioPessoaDto;
import io.freitas.empcard.dto.ResumoCartaoRelatorioDto;
import io.freitas.empcard.model.TipoLancamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integracao para o fluxo de geracao de PDF do relatorio de pessoa.
 */
@SpringBootTest
class PdfRelatorioServiceTests {

    @Autowired
    private PdfRelatorioService pdfRelatorioService;

    /**
     * Valida que o servico gera um PDF valido ao processar o template com formatacao monetaria.
     * Este teste protege especificamente o uso de variaveis de formatacao dentro do template
     * do PDF, evitando regressao de falhas de avaliacao SpEL em tempo de execucao.
     */
    @Test
    void deveGerarPdfRelatorioPessoaComSucesso() {
        // Monta um item simples para exercitar a renderizacao da tabela no template.
        ItemRelatorioDto item = new ItemRelatorioDto(
                "Compra mercado",
                TipoLancamento.AVULSO,
                "1/1",
                new BigDecimal("120.50"),
                "Sem observacao");

        // Consolida os totais do cartao para cobrir os campos monetarios do resumo.
        ResumoCartaoRelatorioDto cartao = new ResumoCartaoRelatorioDto(
                1L,
                "1234 5678 9012 3456",
                "VISA",
                "Banco Teste",
                LocalDate.of(2026, 3, 10),
                new BigDecimal("50.00"),
                new BigDecimal("120.50"),
                new BigDecimal("80.00"),
                new BigDecimal("30.00"),
                new BigDecimal("230.50"),
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("240.50"),
                new BigDecimal("190.50"),
                List.of(item));

        // Cria o DTO raiz com totais gerais para exercitar o bloco final do template.
        RelatorioPessoaDto relatorio = new RelatorioPessoaDto(
                1L,
                "Pessoa Teste",
                "000.000.000-00",
                YearMonth.of(2026, 3),
                List.of(cartao),
                new BigDecimal("240.50"),
                new BigDecimal("50.00"),
                new BigDecimal("190.50"));

        byte[] pdfGerado = pdfRelatorioService.gerarPdfRelatorioPessoa(relatorio);

        assertThat(pdfGerado).isNotNull().isNotEmpty();
        assertThat(new String(pdfGerado, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
    }
}
