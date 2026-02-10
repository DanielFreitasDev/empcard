package io.freitas.empcard.controller;

import io.freitas.empcard.config.VisualizacaoMobileService;
import io.freitas.empcard.dto.RelatorioPessoaDto;
import io.freitas.empcard.service.PdfRelatorioService;
import io.freitas.empcard.service.PessoaService;
import io.freitas.empcard.service.RelatorioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;

/**
 * Controlador de relatorios mensais por pessoa com exportacao em PDF.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/relatorios")
public class RelatorioController {

    private final PessoaService pessoaService;
    private final RelatorioService relatorioService;
    private final PdfRelatorioService pdfRelatorioService;
    private final VisualizacaoMobileService visualizacaoMobileService;

    /**
     * Exibe filtro e resultado do relatorio analitico mensal por pessoa.
     *
     * @param pessoaId         id da pessoa selecionada
     * @param competenciaTexto competencia no formato yyyy-MM
     * @param model            modelo da tela
     * @param requisicao       requisicao HTTP para deteccao de layout mobile
     * @return template de relatorio
     */
    @GetMapping("/pessoas")
    public String relatorioPessoas(@RequestParam(required = false) Long pessoaId,
                                   @RequestParam(required = false) String competenciaTexto,
                                   Model model,
                                   HttpServletRequest requisicao) {
        model.addAttribute("pessoas", pessoaService.listarAtivos());

        YearMonth competencia = (competenciaTexto == null || competenciaTexto.isBlank())
                ? YearMonth.now()
                : YearMonth.parse(competenciaTexto);

        model.addAttribute("competenciaSelecionada", competencia);
        model.addAttribute("pessoaIdSelecionada", pessoaId);

        if (pessoaId != null) {
            RelatorioPessoaDto relatorio = relatorioService.gerarRelatorioPessoa(pessoaId, competencia);
            model.addAttribute("relatorio", relatorio);
        }

        return visualizacaoMobileService.resolverTemplate(requisicao, "relatorios/pessoa", "mobile/relatorios/pessoa");
    }

    /**
     * Exporta PDF analitico mensal de pessoa para envio externo.
     *
     * @param pessoaId         id da pessoa
     * @param competenciaTexto competencia no formato yyyy-MM
     * @return arquivo PDF em anexo
     */
    @GetMapping("/pessoas/{pessoaId}/pdf")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable Long pessoaId,
                                              @RequestParam String competenciaTexto) {
        YearMonth competencia = YearMonth.parse(competenciaTexto);
        RelatorioPessoaDto relatorio = relatorioService.gerarRelatorioPessoa(pessoaId, competencia);
        byte[] arquivo = pdfRelatorioService.gerarPdfRelatorioPessoa(relatorio);

        String nomeArquivo = "relatorio-" + relatorio.nomePessoa().replace(' ', '-') + "-" + competencia + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(nomeArquivo, StandardCharsets.UTF_8)
                .build());

        log.info("PDF disponibilizado para download. pessoaId={}, competencia={}", pessoaId, competencia);
        return ResponseEntity.ok().headers(headers).body(arquivo);
    }
}
