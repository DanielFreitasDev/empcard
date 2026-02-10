package io.freitas.empcard.controller;

import io.freitas.empcard.config.VisualizacaoMobileService;
import io.freitas.empcard.dto.CartaoFormDto;
import io.freitas.empcard.model.Cartao;
import io.freitas.empcard.service.CartaoService;
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
 * Fluxo web de cadastro e manutencao de cartoes.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cartoes")
public class CartaoController {

    private final CartaoService cartaoService;
    private final VisualizacaoMobileService visualizacaoMobileService;

    /**
     * Exibe listagem de cartoes.
     *
     * @param model      modelo da tela
     * @param requisicao requisicao HTTP para deteccao de layout mobile
     * @return template de listagem
     */
    @GetMapping
    public String listar(Model model, HttpServletRequest requisicao) {
        model.addAttribute("cartoes", cartaoService.listarTodos());
        return visualizacaoMobileService.resolverTemplate(requisicao, "cartoes/lista", "mobile/cartoes/lista");
    }

    /**
     * Exibe formulario de novo cartao.
     *
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CartaoFormDto());
        }
        model.addAttribute("acao", "salvar");
        model.addAttribute("titulo", "Novo Cartao");
        return "cartoes/form";
    }

    /**
     * Persiste novo cartao.
     *
     * @param form               dados validados
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") CartaoFormDto form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("acao", "salvar");
            model.addAttribute("titulo", "Novo Cartao");
            return "cartoes/form";
        }

        cartaoService.criar(form);
        redirectAttributes.addFlashAttribute("sucesso", "Cartao cadastrado com sucesso.");
        return "redirect:/cartoes";
    }

    /**
     * Exibe tela de consulta de cartao.
     *
     * @param id    id do cartao
     * @param model modelo da tela
     * @return template de detalhe
     */
    @GetMapping("/{id}/visualizar")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("cartao", cartaoService.buscarPorId(id));
        return "cartoes/detalhe";
    }

    /**
     * Exibe formulario de edicao de cartao.
     *
     * @param id    id do cartao
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Cartao cartao = cartaoService.buscarPorId(id);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", cartaoService.paraForm(cartao));
        }
        model.addAttribute("cartao", cartao);
        model.addAttribute("acao", "atualizar");
        model.addAttribute("titulo", "Editar Cartao");
        return "cartoes/form";
    }

    /**
     * Atualiza cartao existente.
     *
     * @param id                 id do cartao
     * @param form               dados do formulario
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") CartaoFormDto form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cartao", cartaoService.buscarPorId(id));
            model.addAttribute("acao", "atualizar");
            model.addAttribute("titulo", "Editar Cartao");
            return "cartoes/form";
        }

        cartaoService.atualizar(id, form);
        redirectAttributes.addFlashAttribute("sucesso", "Cartao atualizado com sucesso.");
        return "redirect:/cartoes";
    }

    /**
     * Alterna status ativo/inativo do cartao.
     *
     * @param id                 id do cartao
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cartaoService.alternarAtivo(id);
        redirectAttributes.addFlashAttribute("sucesso", "Status do cartao alterado com sucesso.");
        return "redirect:/cartoes";
    }

    /**
     * Exclui cartao quando sem vinculacoes.
     *
     * @param id                 id do cartao
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cartaoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Cartao excluido com sucesso.");
        return "redirect:/cartoes";
    }
}
