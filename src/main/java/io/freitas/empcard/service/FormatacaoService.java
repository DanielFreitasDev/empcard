package io.freitas.empcard.service;

import io.freitas.empcard.util.DocumentoUtils;
import io.freitas.empcard.util.ValorMonetarioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servico utilitario exposto ao Thymeleaf para mascaras e formatacao monetaria.
 */
@Service("formatacaoService")
public class FormatacaoService {

    /**
     * Formata valor como moeda brasileira com simbolo R$.
     *
     * @param valor valor decimal
     * @return texto formatado
     */
    public String moeda(BigDecimal valor) {
        return ValorMonetarioUtils.formatar(valor);
    }

    /**
     * Mascara CPF no padrao XXX.XXX.XXX-XX.
     *
     * @param cpf cpf sem mascara
     * @return cpf mascarado
     */
    public String cpf(String cpf) {
        return DocumentoUtils.mascararCpf(cpf);
    }

    /**
     * Mascara numero do cartao em blocos de 4 digitos.
     *
     * @param numero numero sem mascara
     * @return numero mascarado
     */
    public String cartao(String numero) {
        return DocumentoUtils.mascararCartao(numero);
    }
}
