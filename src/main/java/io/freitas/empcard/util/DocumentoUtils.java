package io.freitas.empcard.util;

/**
 * Utilitarios para tratar documentos e mascaras exibidas no frontend.
 */
public final class DocumentoUtils {

    private DocumentoUtils() {
    }

    /**
     * Remove qualquer caractere nao numerico para persistencia limpa no banco.
     *
     * @param valor valor potencialmente mascarado
     * @return somente digitos ou string vazia quando nulo
     */
    public static String somenteDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    /**
     * Aplica mascara padrao de CPF para exibicao no frontend.
     *
     * @param cpfSomenteDigitos cpf com 11 digitos
     * @return CPF mascarado quando valido ou valor original quando invalido
     */
    public static String mascararCpf(String cpfSomenteDigitos) {
        String cpf = somenteDigitos(cpfSomenteDigitos);
        if (cpf.length() != 11) {
            return cpfSomenteDigitos;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    /**
     * Aplica mascara de numero de cartao para a visualizacao de tela.
     *
     * @param numeroSomenteDigitos numero com 16 digitos
     * @return numero mascarado em blocos de 4 ou valor original quando invalido
     */
    public static String mascararCartao(String numeroSomenteDigitos) {
        String numero = somenteDigitos(numeroSomenteDigitos);
        if (numero.length() != 16) {
            return numeroSomenteDigitos;
        }
        return numero.substring(0, 4) + " " + numero.substring(4, 8) + " " + numero.substring(8, 12) + " " + numero.substring(12, 16);
    }
}
