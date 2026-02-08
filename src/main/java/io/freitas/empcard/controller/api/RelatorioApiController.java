package io.freitas.empcard.controller.api;

import io.freitas.empcard.dto.RelatorioPessoaDto;
import io.freitas.empcard.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * API REST para consultas de relatorio mensal.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/relatorios")
@Tag(name = "Relatorios", description = "Consultas de relatorios")
public class RelatorioApiController {

    private final RelatorioService relatorioService;

    /**
     * Retorna relatorio mensal de uma pessoa para integracoes externas.
     *
     * @param pessoaId         id da pessoa
     * @param competenciaTexto competencia no formato yyyy-MM
     * @return relatorio consolidado
     */
    @GetMapping("/pessoas/{pessoaId}")
    @Operation(summary = "Gerar relatorio mensal de pessoa")
    public RelatorioPessoaDto relatorioPessoa(@PathVariable Long pessoaId,
                                              @RequestParam String competenciaTexto) {
        return relatorioService.gerarRelatorioPessoa(pessoaId, YearMonth.parse(competenciaTexto));
    }
}
