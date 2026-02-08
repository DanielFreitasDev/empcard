package io.freitas.empcard.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de autenticacao e tela de login.
 */
@Slf4j
@Controller
public class AutenticacaoController {

    /**
     * Exibe login quando usuario nao autenticado, redirecionando para dashboard em sessao ativa.
     *
     * @param authentication autenticacao atual (pode ser anonima)
     * @return view de login ou redirecionamento
     */
    @GetMapping("/login")
    public String login(Authentication authentication) {
        boolean autenticado = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        if (autenticado) {
            log.info("Usuario ja autenticado acessou /login. Redirecionando para dashboard");
            return "redirect:/dashboard";
        }

        return "auth/login";
    }
}
