package io.freitas.empcard.controller;

import io.freitas.empcard.config.VisualizacaoMobileService;
import io.freitas.empcard.dto.RelatorioPessoaDto;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.service.CartaoService;
import io.freitas.empcard.service.LancamentoService;
import io.freitas.empcard.service.PagamentoService;
import io.freitas.empcard.service.PessoaService;
import io.freitas.empcard.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard principal com visao consolidada do sistema.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PessoaService pessoaService;
    private final CartaoService cartaoService;
    private final LancamentoService lancamentoService;
    private final PagamentoService pagamentoService;
    private final RelatorioService relatorioService;
    private final VisualizacaoMobileService visualizacaoMobileService;

    /**
     * Exibe resumo operacional com indicadores e ranking de maiores saldos em aberto.
     *
     * @param model     modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template do dashboard
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, HttpServletRequest requisicao) {
        YearMonth competenciaAtual = YearMonth.now();

        List<Pessoa> pessoasAtivas = pessoaService.listarAtivos();
        List<ResumoSaldoPessoa> rankingSaldos = new ArrayList<>();

        // Calcula saldo consolidado por pessoa na competencia corrente para priorizar cobranca.
        for (Pessoa pessoa : pessoasAtivas) {
            RelatorioPessoaDto relatorio = relatorioService.gerarRelatorioPessoa(pessoa.getId(), competenciaAtual);
            if (relatorio.totalGeralSaldo().signum() > 0) {
                rankingSaldos.add(new ResumoSaldoPessoa(pessoa.getNome(), relatorio.totalGeralSaldo()));
            }
        }

        rankingSaldos.sort(Comparator.comparing(ResumoSaldoPessoa::saldo).reversed());

        BigDecimal totalEmAberto = rankingSaldos.stream()
                .map(ResumoSaldoPessoa::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("competenciaAtual", competenciaAtual);
        model.addAttribute("totalPessoasAtivas", pessoasAtivas.size());
        model.addAttribute("totalCartoesAtivos", cartaoService.listarAtivos().size());
        model.addAttribute("totalLancamentos", lancamentoService.listarTodos().size());
        model.addAttribute("totalPagamentos", pagamentoService.listarTodos().size());
        model.addAttribute("totalEmAberto", totalEmAberto);
        model.addAttribute("rankingSaldos", rankingSaldos.stream().limit(10).toList());

        log.info("Dashboard carregado para competencia {}", competenciaAtual);
        return visualizacaoMobileService.resolverTemplate(requisicao, "dashboard/index", "mobile/dashboard/index");
    }

    /**
     * Projecao interna para exibir ranking de saldos no dashboard.
     *
     * @param nomePessoa nome da pessoa
     * @param saldo      saldo em aberto
     */
    private record ResumoSaldoPessoa(String nomePessoa, BigDecimal saldo) {
    }
}
