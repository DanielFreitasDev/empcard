package io.freitas.empcard.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.freitas.empcard.dto.RelatorioPessoaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

/**
 * Converte relatorios renderizados em HTML para PDF.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfRelatorioService {

    private final TemplateEngine templateEngine;
    private final FormatacaoService formatacaoService;

    /**
     * Gera PDF do relatorio analitico mensal de pessoa.
     *
     * @param relatorio dados consolidados do relatorio
     * @return bytes do arquivo PDF
     */
    public byte[] gerarPdfRelatorioPessoa(RelatorioPessoaDto relatorio) {
        try {
            Context contexto = new Context();
            contexto.setVariable("relatorio", relatorio);
            contexto.setVariable("formatacaoService", formatacaoService);

            // Renderiza o template HTML dedicado para PDF com os dados da competencia.
            String html = templateEngine.process("relatorios/pessoa-pdf", contexto);

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            // Converte HTML em PDF preservando layout de tabela para envio ao devedor.
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();

            log.info("PDF de relatorio gerado com sucesso. pessoaId={}, competencia={}",
                    relatorio.pessoaId(), relatorio.competencia());
            return output.toByteArray();
        } catch (Exception ex) {
            log.error("Falha ao gerar PDF do relatorio. pessoaId={}, competencia={}",
                    relatorio.pessoaId(), relatorio.competencia(), ex);
            throw new IllegalStateException("Nao foi possivel gerar o PDF do relatorio");
        }
    }
}
