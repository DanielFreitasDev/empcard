package io.freitas.empcard.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilitarios para conversao e formatacao monetaria no padrao brasileiro.
 */
public final class ValorMonetarioUtils {

    private static final Locale LOCALE_PT_BR = Locale.of("pt", "BR");

    private ValorMonetarioUtils() {
    }

    /**
     * Converte um texto monetario (com ou sem simbolo de moeda) em BigDecimal.
     *
     * @param valorFormatado texto digitado no frontend
     * @return valor decimal com escala 2
     */
    public static BigDecimal parse(String valorFormatado) {
        if (valorFormatado == null || valorFormatado.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // Remove simbolos para aceitar entradas como "R$ 1.234,56", "R$ 1.234,56" ou "1234,56".
        String limpo = valorFormatado
                .trim()
                .replaceAll("[^\\d,.-]", "");

        if (limpo.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }

        return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formata valor decimal como moeda brasileira com simbolo "R$".
     *
     * @param valor valor decimal
     * @return texto formatado no padrao pt-BR
     */
    public static String formatar(BigDecimal valor) {
        NumberFormat formatador = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;
        return formatador.format(valorSeguro);
    }
}
