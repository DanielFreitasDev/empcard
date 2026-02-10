package io.freitas.empcard.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Resolve dinamicamente qual template deve ser renderizado (desktop ou mobile)
 * com base no contexto da requisicao HTTP.
 */
@Service
public class VisualizacaoMobileService {

    private static final Pattern PADRAO_USER_AGENT_MOBILE = Pattern.compile(
            "android|iphone|ipad|ipod|windows phone|blackberry|opera mini|mobile",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> VALORES_VERDADE = Set.of("1", "true", "sim", "on");
    private static final Set<String> VALORES_FALSO = Set.of("0", "false", "nao", "off");

    /**
     * Decide qual template usar para a resposta atual.
     *
     * @param requisicao      requisicao HTTP recebida pelo controlador
     * @param templateDesktop template padrao para navegacao desktop
     * @param templateMobile  template dedicado para navegacao mobile
     * @return template final a ser renderizado
     */
    public String resolverTemplate(HttpServletRequest requisicao, String templateDesktop, String templateMobile) {
        if (isAcessoMobile(requisicao)) {
            return templateMobile;
        }
        return templateDesktop;
    }

    /**
     * Identifica se a requisicao foi iniciada por um dispositivo mobile.
     *
     * <p>Ordem de avaliacao:</p>
     * <p>1) parametro explicito "mobile" na query string,</p>
     * <p>2) cabecalho "Sec-CH-UA-Mobile",</p>
     * <p>3) deteccao por "User-Agent".</p>
     *
     * @param requisicao requisicao HTTP atual
     * @return true quando o acesso deve usar o layout mobile
     */
    public boolean isAcessoMobile(HttpServletRequest requisicao) {
        if (requisicao == null) {
            return false;
        }

        // Permite forcar ou desativar o modo mobile manualmente para testes.
        Boolean overrideMobile = resolverOverrideMobile(requisicao.getParameter("mobile"));
        if (overrideMobile != null) {
            return overrideMobile;
        }

        // Usa Client Hints quando o navegador envia o indicador mobile explicito.
        String cabecalhoUaMobile = normalizarTexto(requisicao.getHeader("Sec-CH-UA-Mobile"));
        if ("?1".equals(cabecalhoUaMobile)) {
            return true;
        }
        if ("?0".equals(cabecalhoUaMobile)) {
            return false;
        }

        // Fallback por assinatura do User-Agent para navegadores mais antigos.
        String userAgent = normalizarTexto(requisicao.getHeader("User-Agent"));
        return PADRAO_USER_AGENT_MOBILE.matcher(userAgent).find();
    }

    /**
     * Converte um texto possivelmente nulo para uma representacao segura em minusculas.
     *
     * @param texto valor de entrada
     * @return texto normalizado, nunca nulo
     */
    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Interpreta o parametro manual "mobile" aceitando sinonimos de verdadeiro/falso.
     *
     * @param valorParametro valor vindo da query string
     * @return true/false quando reconhecido, ou null quando nao houver override
     */
    private Boolean resolverOverrideMobile(String valorParametro) {
        String valorNormalizado = normalizarTexto(valorParametro);
        if (VALORES_VERDADE.contains(valorNormalizado)) {
            return true;
        }
        if (VALORES_FALSO.contains(valorNormalizado)) {
            return false;
        }
        return null;
    }
}
