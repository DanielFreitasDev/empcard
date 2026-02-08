package io.freitas.empcard.util;

import java.util.Locale;

/**
 * Utilitarios de normalizacao textual para cumprir regras de uppercase no cadastro.
 */
public final class TextoUtils {

    private TextoUtils() {
    }

    /**
     * Aplica trim e uppercase no idioma pt-BR para persistir padrao consistente.
     *
     * @param valor texto de entrada
     * @return texto normalizado ou null quando vazio
     */
    public static String normalizarMaiusculo(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        if (texto.isEmpty()) {
            return null;
        }
        return texto.toUpperCase(Locale.of("pt", "BR"));
    }

    /**
     * Aplica trim sem uppercase para campos que precisam preservar caixa (ex.: e-mail).
     *
     * @param valor texto de entrada
     * @return texto limpo ou null quando vazio
     */
    public static String normalizarSimples(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}
