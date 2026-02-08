package io.freitas.empcard.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Trata excecoes para paginas Thymeleaf mantendo resposta amigavel no frontend.
 */
@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class TratadorGlobalMvcException {

    /**
     * Trata erros conhecidos de dominio e exibe pagina padrao de erro funcional.
     *
     * @param ex      excecao de negocio
     * @param request requisicao em processamento
     * @param model   modelo de resposta
     * @return template de erro funcional
     */
    @ExceptionHandler({RecursoNaoEncontradoException.class, RegraDeNegocioException.class})
    public String tratarExcecoesConhecidas(RuntimeException ex, HttpServletRequest request, Model model) {
        log.warn("Erro funcional no MVC: path={}, mensagem={}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("titulo", "Falha de operacao");
        model.addAttribute("mensagem", ex.getMessage());
        return "error/erro";
    }

    /**
     * Trata excecoes nao previstas e exibe mensagem sem detalhes sensiveis.
     *
     * @param ex      excecao inesperada
     * @param request requisicao em processamento
     * @param model   modelo de resposta
     * @return template de erro generico
     */
    @ExceptionHandler(Exception.class)
    public String tratarExcecaoGenerica(Exception ex, HttpServletRequest request, Model model) {
        log.error("Erro inesperado no MVC: path={}", request.getRequestURI(), ex);
        model.addAttribute("titulo", "Erro interno");
        model.addAttribute("mensagem", "Nao foi possivel concluir a operacao solicitada.");
        return "error/erro";
    }
}
