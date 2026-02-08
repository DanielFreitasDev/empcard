package io.freitas.empcard.controller;

import io.freitas.empcard.dto.PessoaFormDto;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Fluxo web de cadastro, consulta e manutencao de pessoas.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    /**
     * Lista pessoas cadastradas exibindo botoes de visualizar, editar, desativar e excluir.
     *
     * @param model modelo da tela
     * @return template de listagem
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pessoas", pessoaService.listarTodos());
        return "pessoas/lista";
    }

    /**
     * Exibe formulario de nova pessoa.
     *
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PessoaFormDto());
        }
        model.addAttribute("acao", "salvar");
        model.addAttribute("titulo", "Nova Pessoa");
        return "pessoas/form";
    }

    /**
     * Persiste nova pessoa e retorna para listagem.
     *
     * @param form               dados validados
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") PessoaFormDto form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("acao", "salvar");
            model.addAttribute("titulo", "Nova Pessoa");
            return "pessoas/form";
        }

        pessoaService.criar(form);
        redirectAttributes.addFlashAttribute("sucesso", "Pessoa cadastrada com sucesso.");
        return "redirect:/pessoas";
    }

    /**
     * Exibe tela somente leitura com dados completos da pessoa.
     *
     * @param id    id da pessoa
     * @param model modelo da tela
     * @return template de visualizacao
     */
    @GetMapping("/{id}/visualizar")
    public String visualizar(@PathVariable Long id, Model model) {
        Pessoa pessoa = pessoaService.buscarPorId(id);
        model.addAttribute("pessoa", pessoa);
        return "pessoas/detalhe";
    }

    /**
     * Exibe formulario de edicao de pessoa existente.
     *
     * @param id    id da pessoa
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Pessoa pessoa = pessoaService.buscarPorId(id);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", pessoaService.paraForm(pessoa));
        }
        model.addAttribute("pessoa", pessoa);
        model.addAttribute("acao", "atualizar");
        model.addAttribute("titulo", "Editar Pessoa");
        return "pessoas/form";
    }

    /**
     * Atualiza dados da pessoa e retorna para listagem.
     *
     * @param id                 id da pessoa
     * @param form               dados do formulario
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") PessoaFormDto form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pessoa", pessoaService.buscarPorId(id));
            model.addAttribute("acao", "atualizar");
            model.addAttribute("titulo", "Editar Pessoa");
            return "pessoas/form";
        }

        pessoaService.atualizar(id, form);
        redirectAttributes.addFlashAttribute("sucesso", "Pessoa atualizada com sucesso.");
        return "redirect:/pessoas";
    }

    /**
     * Alterna status ativo/inativo da pessoa.
     *
     * @param id                 id da pessoa
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pessoaService.alternarAtivo(id);
        redirectAttributes.addFlashAttribute("sucesso", "Status da pessoa alterado com sucesso.");
        return "redirect:/pessoas";
    }

    /**
     * Exclui pessoa quando permitido pelas regras de vinculacao.
     *
     * @param id                 id da pessoa
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pessoaService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Pessoa excluida com sucesso.");
        return "redirect:/pessoas";
    }
}
