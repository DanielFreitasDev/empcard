package io.freitas.empcard.controller;

import io.freitas.empcard.config.VisualizacaoMobileService;
import io.freitas.empcard.dto.LancamentoFormDto;
import io.freitas.empcard.model.Lancamento;
import io.freitas.empcard.model.TipoLancamento;
import io.freitas.empcard.service.CartaoService;
import io.freitas.empcard.service.LancamentoService;
import io.freitas.empcard.service.PessoaService;
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

/**
 * Fluxo web de cadastro e manutencao de lancamentos.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/lancamentos")
public class LancamentoController {

    private final LancamentoService lancamentoService;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;
    private final VisualizacaoMobileService visualizacaoMobileService;

    /**
     * Lista lancamentos ja registrados.
     *
     * @param model modelo da tela
     * @return template de listagem
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lancamentos", lancamentoService.listarTodos());
        return "lancamentos/lista";
    }

    /**
     * Exibe formulario de novo lancamento.
     *
     * @param model      modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model, HttpServletRequest requisicao) {
        if (!model.containsAttribute("form")) {
            LancamentoFormDto form = new LancamentoFormDto();
            form.setTipo(TipoLancamento.AVULSO);
            form.setQuantidadeParcelas(1);
            model.addAttribute("form", form);
        }
        carregarCombos(model);
        model.addAttribute("tipos", TipoLancamento.values());
        model.addAttribute("acao", "salvar");
        model.addAttribute("titulo", "Novo Lancamento");
        return visualizacaoMobileService.resolverTemplate(requisicao, "lancamentos/form", "mobile/lancamentos/form");
    }

    /**
     * Salva novo lancamento.
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
    public String salvar(@Valid @ModelAttribute("form") LancamentoFormDto form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest requisicao) {
        if (bindingResult.hasErrors()) {
            carregarCombos(model);
            model.addAttribute("tipos", TipoLancamento.values());
            model.addAttribute("acao", "salvar");
            model.addAttribute("titulo", "Novo Lancamento");
            return visualizacaoMobileService.resolverTemplate(requisicao, "lancamentos/form", "mobile/lancamentos/form");
        }

        lancamentoService.criar(form);
        redirectAttributes.addFlashAttribute("sucesso", "Lancamento cadastrado com sucesso.");
        return "redirect:/lancamentos";
    }

    /**
     * Exibe detalhes de um lancamento.
     *
     * @param id    id do lancamento
     * @param model modelo da tela
     * @return template de detalhe
     */
    @GetMapping("/{id}/visualizar")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("lancamento", lancamentoService.buscarPorId(id));
        return "lancamentos/detalhe";
    }

    /**
     * Exibe formulario de edicao de lancamento.
     *
     * @param id         id do lancamento
     * @param model      modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, HttpServletRequest requisicao) {
        Lancamento lancamento = lancamentoService.buscarPorId(id);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", lancamentoService.paraForm(lancamento));
        }
        carregarCombos(model);
        model.addAttribute("tipos", TipoLancamento.values());
        model.addAttribute("lancamento", lancamento);
        model.addAttribute("acao", "atualizar");
        model.addAttribute("titulo", "Editar Lancamento");
        return visualizacaoMobileService.resolverTemplate(requisicao, "lancamentos/form", "mobile/lancamentos/form");
    }

    /**
     * Atualiza lancamento existente.
     *
     * @param id                 id do lancamento
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
                            @Valid @ModelAttribute("form") LancamentoFormDto form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest requisicao) {
        if (bindingResult.hasErrors()) {
            carregarCombos(model);
            model.addAttribute("tipos", TipoLancamento.values());
            model.addAttribute("lancamento", lancamentoService.buscarPorId(id));
            model.addAttribute("acao", "atualizar");
            model.addAttribute("titulo", "Editar Lancamento");
            return visualizacaoMobileService.resolverTemplate(requisicao, "lancamentos/form", "mobile/lancamentos/form");
        }

        lancamentoService.atualizar(id, form);
        redirectAttributes.addFlashAttribute("sucesso", "Lancamento atualizado com sucesso.");
        return "redirect:/lancamentos";
    }

    /**
     * Alterna status ativo/inativo do lancamento.
     *
     * @param id                 id do lancamento
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lancamentoService.alternarAtivo(id);
        redirectAttributes.addFlashAttribute("sucesso", "Status do lancamento alterado com sucesso.");
        return "redirect:/lancamentos";
    }

    /**
     * Exclui lancamento definitivamente.
     *
     * @param id                 id do lancamento
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lancamentoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Lancamento excluido com sucesso.");
        return "redirect:/lancamentos";
    }

    /**
     * Carrega combos comuns da tela para pessoa e cartao.
     *
     * @param model modelo da pagina
     */
    private void carregarCombos(Model model) {
        model.addAttribute("pessoas", pessoaService.listarTodos());
        model.addAttribute("cartoes", cartaoService.listarTodos());
    }
}
