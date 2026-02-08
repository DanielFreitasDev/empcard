package io.freitas.empcard.util;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Regras de calendario para determinar competencia e vencimento de faturas.
 */
public final class DataCompetenciaUtils {

    private DataCompetenciaUtils() {
    }

    /**
     * Calcula a competencia da fatura com base na data da compra e no dia de fechamento.
     * Regra aplicada: compras no dia de fechamento ou depois entram na competencia seguinte.
     *
     * @param dataCompra    data da compra
     * @param diaFechamento dia de fechamento do cartao
     * @return competencia correspondente (ano/mes)
     */
    public static YearMonth calcularCompetenciaInicial(LocalDate dataCompra, int diaFechamento) {
        YearMonth competenciaBase = YearMonth.from(dataCompra);
        if (dataCompra.getDayOfMonth() >= diaFechamento) {
            return competenciaBase.plusMonths(1);
        }
        return competenciaBase;
    }

    /**
     * Calcula data de vencimento considerando se o dia de vencimento vem antes do fechamento.
     * Quando dia de vencimento for menor ou igual ao fechamento, o vencimento cai no mes seguinte.
     *
     * @param competencia   competencia da fatura
     * @param diaFechamento dia de fechamento do cartao
     * @param diaVencimento dia de vencimento configurado
     * @return data de vencimento da competencia
     */
    public static LocalDate calcularDataVencimento(YearMonth competencia, int diaFechamento, int diaVencimento) {
        YearMonth mesBase = diaVencimento <= diaFechamento ? competencia.plusMonths(1) : competencia;
        int diaAjustado = Math.min(diaVencimento, mesBase.lengthOfMonth());
        return mesBase.atDay(diaAjustado);
    }
}
