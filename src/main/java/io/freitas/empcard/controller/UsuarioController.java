package io.freitas.empcard.controller;

import io.freitas.empcard.dto.AlterarSenhaFormDto;
import io.freitas.empcard.dto.ResetSenhaUsuarioDto;
import io.freitas.empcard.dto.UsuarioFormDto;
import io.freitas.empcard.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
 * Controlador web para administracao de usuarios e senhas.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Lista usuarios cadastrados.
     *
     * @param model modelo da tela
     * @return template de listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    /**
     * Exibe formulario de novo usuario.
     *
     * @param model modelo da tela
     * @return template de formulario
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new UsuarioFormDto());
        }
        return "usuarios/form";
    }

    /**
     * Salva novo usuario.
     *
     * @param form               dados do formulario
     * @param bindingResult      erros de validacao
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") UsuarioFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "usuarios/form";
        }

        usuarioService.criarUsuario(form);
        redirectAttributes.addFlashAttribute("sucesso", "Usuario criado com sucesso.");
        return "redirect:/usuarios";
    }

    /**
     * Alterna status ativo/inativo de usuario.
     *
     * @param id                 id do usuario
     * @param redirectAttributes mensagens de retorno
     * @return redirecionamento para listagem
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.alternarAtivo(id);
        redirectAttributes.addFlashAttribute("sucesso", "Status do usuario alterado com sucesso.");
        return "redirect:/usuarios";
    }

    /**
     * Exibe formulario para redefinicao administrativa de senha.
     *
     * @param id    id do usuario
     * @param model modelo da tela
     * @return template de redefinicao
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/senha")
    public String exibirRedefinicaoSenha(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ResetSenhaUsuarioDto());
        }
        return "usuarios/redefinir-senha";
    }

    /**
     * Processa redefinicao administrativa de senha.
     *
     * @param id                 id do usuario
     * @param form               dados de nova senha
     * @param bindingResult      erros de validacao
     * @param model              modelo da tela
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/senha")
    public String redefinirSenha(@PathVariable Long id,
                                 @Valid @ModelAttribute("form") ResetSenhaUsuarioDto form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            return "usuarios/redefinir-senha";
        }

        usuarioService.redefinirSenha(id, form.getNovaSenha());
        redirectAttributes.addFlashAttribute("sucesso", "Senha redefinida com sucesso.");
        return "redirect:/usuarios";
    }

    /**
     * Exibe formulario de alteracao da propria senha para qualquer usuario autenticado.
     *
     * @param model modelo da tela
     * @return template de alteracao
     */
    @GetMapping("/minha-senha")
    public String exibirMinhaSenha(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AlterarSenhaFormDto());
        }
        return "usuarios/minha-senha";
    }

    /**
     * Processa alteracao da senha do proprio usuario autenticado.
     *
     * @param form               dados de senha atual e nova senha
     * @param bindingResult      erros de validacao
     * @param authentication     usuario autenticado
     * @param redirectAttributes mensagens de retorno
     * @return view ou redirecionamento
     */
    @PostMapping("/minha-senha")
    public String alterarMinhaSenha(@Valid @ModelAttribute("form") AlterarSenhaFormDto form,
                                    BindingResult bindingResult,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "usuarios/minha-senha";
        }

        usuarioService.alterarSenhaPropria(authentication.getName(), form);
        redirectAttributes.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
        return "redirect:/dashboard";
    }
}
