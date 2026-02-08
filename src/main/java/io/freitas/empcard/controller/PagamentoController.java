package io.freitas.empcard.controller;

import io.freitas.empcard.dto.PagamentoFormDto;
import io.freitas.empcard.model.Pagamento;
import io.freitas.empcard.service.CartaoService;
import io.freitas.empcard.service.PagamentoService;
import io.freitas.empcard.service.PessoaService;
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

import java.time.LocalDate;

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

    /**
     * Lista pagamentos registrados.
     *
     * @param model modelo da tela
     * @return template de listagem
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagamentos", pagamentoService.listarTodos());
        return "pagamentos/lista";
    }

    /**
     * Exibe formulario de novo pagamento.
     *
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("form")) {
            PagamentoFormDto form = new PagamentoFormDto();
            form.setDataPagamento(LocalDate.now());
            model.addAttribute("form", form);
        }
        carregarCombos(model);
        model.addAttribute("acao", "salvar");
        model.addAttribute("titulo", "Novo Pagamento");
        return "pagamentos/form";
    }

    /**
     * Salva novo pagamento.
     *
     * @param form               dados do formulario
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") PagamentoFormDto form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            carregarCombos(model);
            model.addAttribute("acao", "salvar");
            model.addAttribute("titulo", "Novo Pagamento");
            return "pagamentos/form";
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
     * @param id    id do pagamento
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Pagamento pagamento = pagamentoService.buscarPorId(id);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", pagamentoService.paraForm(pagamento));
        }
        carregarCombos(model);
        model.addAttribute("pagamento", pagamento);
        model.addAttribute("acao", "atualizar");
        model.addAttribute("titulo", "Editar Pagamento");
        return "pagamentos/form";
    }

    /**
     * Atualiza pagamento existente.
     *
     * @param id                 id do pagamento
     * @param form               dados atualizados
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") PagamentoFormDto form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            carregarCombos(model);
            model.addAttribute("pagamento", pagamentoService.buscarPorId(id));
            model.addAttribute("acao", "atualizar");
            model.addAttribute("titulo", "Editar Pagamento");
            return "pagamentos/form";
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
}
