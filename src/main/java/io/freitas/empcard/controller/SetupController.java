package io.freitas.empcard.controller;

import io.freitas.empcard.dto.SetupInicialFormDto;
import io.freitas.empcard.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para configuracao do primeiro usuario administrador.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SetupController {

    private final UsuarioService usuarioService;

    /**
     * Exibe formulario de setup inicial quando ainda nao existe usuario no sistema.
     *
     * @param model modelo da view
     * @return pagina de setup ou redirecionamento para login
     */
    @GetMapping("/setup/inicial")
    public String exibirSetupInicial(Model model) {
        if (usuarioService.possuiUsuarios()) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SetupInicialFormDto());
        }
        return "setup/inicial";
    }

    /**
     * Salva o primeiro usuario administrador da aplicacao.
     *
     * @param form               formulario validado
     * @param bindingResult      resultado de validacao
     * @param redirectAttributes atributos de redirecionamento
     * @return redirecionamento para login ou retorno ao formulario
     */
    @PostMapping("/setup/inicial/salvar")
    public String salvarSetupInicial(@Valid @ModelAttribute("form") SetupInicialFormDto form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "setup/inicial";
        }

        usuarioService.criarPrimeiroAdmin(form);
        log.info("Setup inicial concluido");

        redirectAttributes.addFlashAttribute("sucesso", "Primeiro administrador criado com sucesso. Faca login para continuar.");
        return "redirect:/login";
    }
}
