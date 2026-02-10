package io.freitas.empcard.controller;

import io.freitas.empcard.config.VisualizacaoMobileService;
import io.freitas.empcard.dto.RelatorioPessoaDto;
import io.freitas.empcard.dto.PagamentoFormDto;
import io.freitas.empcard.model.Pagamento;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.service.CartaoService;
import io.freitas.empcard.service.PagamentoService;
import io.freitas.empcard.service.PessoaService;
import io.freitas.empcard.service.RelatorioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Fluxo web de cadastro e manutencao de pagamentos.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;
    private final RelatorioService relatorioService;
    private final VisualizacaoMobileService visualizacaoMobileService;

    /**
     * Lista pagamentos registrados.
     *
     * @param model      modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template de listagem
     */
    @GetMapping
    public String listar(Model model, HttpServletRequest requisicao) {
        model.addAttribute("pagamentos", pagamentoService.listarTodos());

        YearMonth competenciaAtual = YearMonth.now();
        List<ResumoCobrancaMes> cobrancasMobile = construirResumoCobrancas(competenciaAtual);

        BigDecimal totalDevidoMobile = cobrancasMobile.stream()
                .map(ResumoCobrancaMes::totalDevido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEmAbertoMobile = cobrancasMobile.stream()
                .map(ResumoCobrancaMes::saldoAberto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPagasMobile = cobrancasMobile.stream()
                .filter(item -> item.saldoAberto().signum() <= 0)
                .count();

        model.addAttribute("competenciaAtual", competenciaAtual);
        model.addAttribute("cobrancasMobile", cobrancasMobile);
        model.addAttribute("totalDevidoMobile", totalDevidoMobile);
        model.addAttribute("totalEmAbertoMobile", totalEmAbertoMobile);
        model.addAttribute("totalPagasMobile", totalPagasMobile);

        return visualizacaoMobileService.resolverTemplate(requisicao, "pagamentos/lista", "mobile/pagamentos/lista");
    }

    /**
     * Exibe formulario de novo pagamento.
     *
     * @param model      modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model, HttpServletRequest requisicao) {
        if (!model.containsAttribute("form")) {
            PagamentoFormDto form = new PagamentoFormDto();
            form.setDataPagamento(LocalDate.now());
            model.addAttribute("form", form);
        }
        carregarCombos(model);
        model.addAttribute("acao", "salvar");
        model.addAttribute("titulo", "Novo Pagamento");
        return visualizacaoMobileService.resolverTemplate(requisicao, "pagamentos/form", "mobile/pagamentos/form");
    }

    /**
     * Salva novo pagamento.
     *
     * @param form               dados do formulario
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @param requisicao         requisicao HTTP para deteccao de layout mobile
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") PagamentoFormDto form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest requisicao) {
        if (bindingResult.hasErrors()) {
            carregarCombos(model);
            model.addAttribute("acao", "salvar");
            model.addAttribute("titulo", "Novo Pagamento");
            return visualizacaoMobileService.resolverTemplate(requisicao, "pagamentos/form", "mobile/pagamentos/form");
        }

        pagamentoService.criar(form);
        redirectAttributes.addFlashAttribute("sucesso", "Pagamento cadastrado com sucesso.");
        return "redirect:/pagamentos";
    }

    /**
     * Exibe detalhes de pagamento.
     *
     * @param id    id do pagamento
     * @param model modelo da tela
     * @return template de detalhe
     */
    @GetMapping("/{id}/visualizar")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("pagamento", pagamentoService.buscarPorId(id));
        return "pagamentos/detalhe";
    }

    /**
     * Exibe formulario de edicao de pagamento.
     *
     * @param id         id do pagamento
     * @param model      modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, HttpServletRequest requisicao) {
        Pagamento pagamento = pagamentoService.buscarPorId(id);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", pagamentoService.paraForm(pagamento));
        }
        carregarCombos(model);
        model.addAttribute("pagamento", pagamento);
        model.addAttribute("acao", "atualizar");
        model.addAttribute("titulo", "Editar Pagamento");
        return visualizacaoMobileService.resolverTemplate(requisicao, "pagamentos/form", "mobile/pagamentos/form");
    }

    /**
     * Atualiza pagamento existente.
     *
     * @param id                 id do pagamento
     * @param form               dados atualizados
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @param requisicao         requisicao HTTP para deteccao de layout mobile
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") PagamentoFormDto form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest requisicao) {
        if (bindingResult.hasErrors()) {
            carregarCombos(model);
            model.addAttribute("pagamento", pagamentoService.buscarPorId(id));
            model.addAttribute("acao", "atualizar");
            model.addAttribute("titulo", "Editar Pagamento");
            return visualizacaoMobileService.resolverTemplate(requisicao, "pagamentos/form", "mobile/pagamentos/form");
        }

        pagamentoService.atualizar(id, form);
        redirectAttributes.addFlashAttribute("sucesso", "Pagamento atualizado com sucesso.");
        return "redirect:/pagamentos";
    }

    /**
     * Exclui pagamento quando necessario.
     *
     * @param id                 id do pagamento
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pagamentoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Pagamento excluido com sucesso.");
        return "redirect:/pagamentos";
    }

    /**
     * Carrega combos comuns de pessoa e cartao para a tela de pagamento.
     *
     * @param model modelo da tela
     */
    private void carregarCombos(Model model) {
        model.addAttribute("pessoas", pessoaService.listarTodos());
        model.addAttribute("cartoes", cartaoService.listarTodos());
    }

    /**
     * Consolida as cobrancas da competencia para composicao da tela mobile.
     *
     * @param competenciaAtual competencia de referencia
     * @return lista ordenada por maior saldo em aberto
     */
    private List<ResumoCobrancaMes> construirResumoCobrancas(YearMonth competenciaAtual) {
        List<ResumoCobrancaMes> resumos = new ArrayList<>();

        // Avalia cada pessoa ativa para exibir no mobile apenas quem possui movimento no mes.
        for (Pessoa pessoa : pessoaService.listarAtivos()) {
            RelatorioPessoaDto relatorio = relatorioService.gerarRelatorioPessoa(pessoa.getId(), competenciaAtual);

            BigDecimal totalDevido = relatorio.totalGeralDevido();
            BigDecimal totalPago = relatorio.totalGeralPago();
            BigDecimal saldoAberto = relatorio.totalGeralSaldo();

            if (totalDevido.signum() == 0 && totalPago.signum() == 0 && saldoAberto.signum() == 0) {
                continue;
            }

            resumos.add(new ResumoCobrancaMes(
                    pessoa.getId(),
                    pessoa.getNome(),
                    totalDevido,
                    totalPago,
                    saldoAberto
            ));
        }

        resumos.sort(Comparator.comparing(ResumoCobrancaMes::saldoAberto).reversed());
        return resumos;
    }

    /**
     * Projecao de cobranca mensal usada na apresentacao mobile de cobrancas.
     *
     * @param pessoaId    identificador da pessoa
     * @param nomePessoa  nome de exibicao
     * @param totalDevido total devido na competencia
     * @param totalPago   total pago na competencia
     * @param saldoAberto saldo remanescente na competencia
     */
    private record ResumoCobrancaMes(
            Long pessoaId,
            String nomePessoa,
            BigDecimal totalDevido,
            BigDecimal totalPago,
            BigDecimal saldoAberto
    ) {
    }
}
