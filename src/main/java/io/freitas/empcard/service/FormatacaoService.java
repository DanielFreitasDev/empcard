package io.freitas.empcard.service;

import io.freitas.empcard.util.DocumentoUtils;
import io.freitas.empcard.util.ValorMonetarioUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Servico utilitario exposto ao Thymeleaf para mascaras e formatacao monetaria.
 */
@Service("formatacaoService")
public class FormatacaoService {

    private static final DateTimeFormatter FORMATADOR_COMPETENCIA = DateTimeFormatter.ofPattern("MM-uuuu");

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

    /**
     * Formata a competencia para exibicao amigavel no frontend.
     *
     * <p>Internamente o sistema usa o padrao ISO de YearMonth (yyyy-MM) para manter
     * compatibilidade com input month e parse automatico. Na camada visual usamos
     * MM-yyyy para facilitar leitura por usuarios finais.</p>
     *
     * @param competencia referencia de ano e mes
     * @return competencia formatada em MM-yyyy, ou "-" quando nao informada
     */
    public String competencia(YearMonth competencia) {
        // Evita erro de renderizacao em telas/PDF quando a competencia nao vier preenchida.
        if (competencia == null) {
            return "-";
        }

        // Centraliza o padrao para garantir consistencia visual entre todos os templates.
        return competencia.format(FORMATADOR_COMPETENCIA);
    }
}
