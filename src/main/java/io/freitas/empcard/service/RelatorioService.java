package io.freitas.empcard.service;

import io.freitas.empcard.dto.ItemRelatorioDto;
import io.freitas.empcard.dto.RelatorioPessoaDto;
import io.freitas.empcard.dto.ResumoCartaoRelatorioDto;
import io.freitas.empcard.model.Cartao;
import io.freitas.empcard.model.Lancamento;
import io.freitas.empcard.model.Pagamento;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.model.TipoLancamento;
import io.freitas.empcard.repository.LancamentoRepository;
import io.freitas.empcard.repository.PagamentoRepository;
import io.freitas.empcard.util.DataCompetenciaUtils;
import io.freitas.empcard.util.DocumentoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Motor de calculo mensal para consolidacao de dividas por pessoa e cartao.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final PessoaService pessoaService;
    private final LancamentoRepository lancamentoRepository;
    private final PagamentoRepository pagamentoRepository;

    /**
     * Gera relatorio analitico mensal de uma pessoa agrupado por cartao.
     *
     * @param pessoaId    identificador da pessoa
     * @param competencia competencia de referencia (yyyy-MM)
     * @return estrutura consolidada para tela e PDF
     */
    @Transactional(readOnly = true)
    public RelatorioPessoaDto gerarRelatorioPessoa(Long pessoaId, YearMonth competencia) {
        log.info("Gerando relatorio mensal. pessoaId={}, competencia={}", pessoaId, competencia);

        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);
        List<Lancamento> todosLancamentos = lancamentoRepository.findByPessoaIdOrderByDataCompraAsc(pessoaId);
        List<Pagamento> todosPagamentos = pagamentoRepository.findByPessoaIdOrderByDataPagamentoAsc(pessoaId);

        // Descobre os cartoes com qualquer historico da pessoa para montar consolidacao por cartao.
        Map<Long, Cartao> mapaCartoes = new LinkedHashMap<>();
        todosLancamentos.forEach(lancamento -> mapaCartoes.put(lancamento.getCartao().getId(), lancamento.getCartao()));
        todosPagamentos.forEach(pagamento -> mapaCartoes.put(pagamento.getCartao().getId(), pagamento.getCartao()));

        List<ResumoCartaoRelatorioDto> cartoes = mapaCartoes.values().stream()
                .sorted(Comparator.comparing(Cartao::getBanco, Comparator.nullsLast(String::compareTo)))
                .map(cartao -> calcularResumoCartao(competencia, pessoa, cartao, todosLancamentos, todosPagamentos))
                .filter(Objects::nonNull)
                .toList();

        BigDecimal totalDevido = somar(cartoes.stream().map(ResumoCartaoRelatorioDto::totalDevido).toList());
        BigDecimal totalPago = somar(cartoes.stream().map(ResumoCartaoRelatorioDto::totalPagamentos).toList());
        BigDecimal totalSaldo = somar(cartoes.stream().map(ResumoCartaoRelatorioDto::saldoFinal).toList());

        return new RelatorioPessoaDto(
                pessoa.getId(),
                pessoa.getNome(),
                DocumentoUtils.mascararCpf(pessoa.getCpf()),
                competencia,
                cartoes,
                totalDevido,
                totalPago,
                totalSaldo
        );
    }

    /**
     * Consolida valores de uma pessoa em um cartao para uma competencia especifica.
     *
     * @param competencia      competencia solicitada
     * @param pessoa           pessoa de referencia
     * @param cartao           cartao em consolidacao
     * @param todosLancamentos lista completa de lancamentos da pessoa
     * @param todosPagamentos  lista completa de pagamentos da pessoa
     * @return resumo por cartao ou null quando nao ha atividade relevante
     */
    private ResumoCartaoRelatorioDto calcularResumoCartao(YearMonth competencia,
                                                          Pessoa pessoa,
                                                          Cartao cartao,
                                                          List<Lancamento> todosLancamentos,
                                                          List<Pagamento> todosPagamentos) {
        List<Lancamento> lancamentosCartao = todosLancamentos.stream()
                .filter(lancamento -> lancamento.getCartao().getId().equals(cartao.getId()))
                .toList();

        List<Pagamento> pagamentosCartao = todosPagamentos.stream()
                .filter(pagamento -> pagamento.getCartao().getId().equals(cartao.getId()))
                .toList();

        YearMonth primeiraCompetencia = obterPrimeiraCompetencia(lancamentosCartao, pagamentosCartao, cartao, competencia);

        BigDecimal saldoAnterior = BigDecimal.ZERO;
        BigDecimal saldoAnteriorCompetencia = BigDecimal.ZERO;
        BigDecimal totalComprasCompetencia = BigDecimal.ZERO;
        BigDecimal jurosMultaCompetencia = BigDecimal.ZERO;
        BigDecimal pagamentosCompetencia = BigDecimal.ZERO;
        BigDecimal saldoFinalCompetencia = BigDecimal.ZERO;
        List<ItemRelatorioDto> itensCompetencia = List.of();

        YearMonth mesAtual = primeiraCompetencia;
        while (!mesAtual.isAfter(competencia)) {
            List<ItemRelatorioDto> itensDoMes = extrairItensDaCompetencia(lancamentosCartao, cartao, mesAtual);
            BigDecimal totalComprasMes = somar(itensDoMes.stream().map(ItemRelatorioDto::valor).toList());
            BigDecimal jurosMultaMes = calcularJurosEMulta(saldoAnterior, pessoa.getJurosMensal(), pessoa.getMultaAtraso());
            BigDecimal pagamentosMes = somarPagamentosMes(pagamentosCartao, mesAtual);
            BigDecimal totalDevidoMes = saldoAnterior.add(totalComprasMes).add(jurosMultaMes);
            BigDecimal saldoFinalMes = totalDevidoMes.subtract(pagamentosMes).setScale(2, RoundingMode.HALF_UP);

            // Guarda os valores da competencia solicitada para exibicao em tela e PDF.
            if (mesAtual.equals(competencia)) {
                saldoAnteriorCompetencia = saldoAnterior;
                totalComprasCompetencia = totalComprasMes;
                jurosMultaCompetencia = jurosMultaMes;
                pagamentosCompetencia = pagamentosMes;
                saldoFinalCompetencia = saldoFinalMes;
                itensCompetencia = itensDoMes;
            }

            saldoAnterior = saldoFinalMes;
            mesAtual = mesAtual.plusMonths(1);
        }

        BigDecimal totalAvulso = somarPorTipo(itensCompetencia, TipoLancamento.AVULSO);
        BigDecimal totalParcelado = somarPorTipo(itensCompetencia, TipoLancamento.PARCELADO);
        BigDecimal totalFixo = somarPorTipo(itensCompetencia, TipoLancamento.FIXO);
        BigDecimal totalDevidoCompetencia = saldoAnteriorCompetencia.add(totalComprasCompetencia).add(jurosMultaCompetencia);

        // Quando nao existe historico para o cartao, nao poluir relatorio com bloco vazio.
        boolean possuiMovimento = totalDevidoCompetencia.signum() != 0
                || pagamentosCompetencia.signum() != 0
                || !itensCompetencia.isEmpty();

        if (!possuiMovimento) {
            return null;
        }

        LocalDate vencimento = DataCompetenciaUtils.calcularDataVencimento(
                competencia,
                cartao.getDiaFechamento(),
                cartao.getDiaVencimento()
        );

        return new ResumoCartaoRelatorioDto(
                cartao.getId(),
                mascararParcialCartao(cartao.getNumero()),
                cartao.getBandeira(),
                cartao.getBanco(),
                vencimento,
                saldoAnteriorCompetencia,
                totalAvulso,
                totalParcelado,
                totalFixo,
                totalComprasCompetencia,
                jurosMultaCompetencia,
                pagamentosCompetencia,
                totalDevidoCompetencia,
                saldoFinalCompetencia,
                itensCompetencia
        );
    }

    /**
     * Determina primeira competencia para iniciar simulacao de saldo de um cartao.
     *
     * @param lancamentos       lancamentos do cartao
     * @param pagamentos        pagamentos do cartao
     * @param cartao            cartao com regra de fechamento
     * @param competenciaPadrao competencia alvo quando nao houver historico
     * @return primeira competencia conhecida
     */
    private YearMonth obterPrimeiraCompetencia(List<Lancamento> lancamentos,
                                               List<Pagamento> pagamentos,
                                               Cartao cartao,
                                               YearMonth competenciaPadrao) {
        List<YearMonth> competencias = new ArrayList<>();

        lancamentos.forEach(lancamento -> competencias.add(
                DataCompetenciaUtils.calcularCompetenciaInicial(lancamento.getDataCompra(), cartao.getDiaFechamento())
        ));
        pagamentos.forEach(pagamento -> competencias.add(YearMonth.from(pagamento.getDataPagamento())));

        return competencias.stream().min(YearMonth::compareTo).orElse(competenciaPadrao);
    }

    /**
     * Extrai itens cobrados em uma competencia considerando regras de avulso, parcelado e fixo.
     *
     * @param lancamentos lancamentos do cartao
     * @param cartao      cartao com dia de fechamento
     * @param competencia competencia avaliada
     * @return lista de itens cobrados no mes
     */
    private List<ItemRelatorioDto> extrairItensDaCompetencia(List<Lancamento> lancamentos, Cartao cartao, YearMonth competencia) {
        List<ItemRelatorioDto> itens = new ArrayList<>();

        for (Lancamento lancamento : lancamentos) {
            YearMonth competenciaInicial = DataCompetenciaUtils.calcularCompetenciaInicial(
                    lancamento.getDataCompra(),
                    cartao.getDiaFechamento()
            );

            if (lancamento.getTipo() == TipoLancamento.AVULSO && competencia.equals(competenciaInicial)) {
                itens.add(new ItemRelatorioDto(
                        lancamento.getDescricao(),
                        TipoLancamento.AVULSO,
                        "1/1",
                        lancamento.getValorTotal(),
                        lancamento.getObservacao()
                ));
            }

            if (lancamento.getTipo() == TipoLancamento.PARCELADO) {
                long meses = ChronoUnit.MONTHS.between(competenciaInicial, competencia);
                if (meses >= 0 && meses < lancamento.getQuantidadeParcelas()) {
                    int parcelaAtual = (int) meses + 1;
                    BigDecimal valorParcela = calcularValorParcela(
                            lancamento.getValorTotal(),
                            lancamento.getQuantidadeParcelas(),
                            parcelaAtual
                    );
                    itens.add(new ItemRelatorioDto(
                            lancamento.getDescricao(),
                            TipoLancamento.PARCELADO,
                            parcelaAtual + "/" + lancamento.getQuantidadeParcelas(),
                            valorParcela,
                            lancamento.getObservacao()
                    ));
                }
            }

            if (lancamento.getTipo() == TipoLancamento.FIXO) {
                boolean iniciou = !competencia.isBefore(competenciaInicial);
                boolean dentroDoFim = lancamento.getDataFimFixo() == null
                        || !competencia.isAfter(YearMonth.from(lancamento.getDataFimFixo()));

                if (iniciou && dentroDoFim) {
                    itens.add(new ItemRelatorioDto(
                            lancamento.getDescricao(),
                            TipoLancamento.FIXO,
                            "FIXO",
                            lancamento.getValorTotal(),
                            lancamento.getObservacao()
                    ));
                }
            }
        }

        return itens;
    }

    /**
     * Calcula juros + multa da competencia corrente com base no saldo anterior em aberto.
     *
     * @param saldoAnterior saldo acumulado do mes anterior
     * @param jurosMensal   percentual de juros da pessoa
     * @param multaAtraso   percentual de multa da pessoa
     * @return valor total de encargos do mes
     */
    private BigDecimal calcularJurosEMulta(BigDecimal saldoAnterior, BigDecimal jurosMensal, BigDecimal multaAtraso) {
        if (saldoAnterior.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal juros = saldoAnterior
                .multiply(jurosMensal)
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);

        BigDecimal multa = saldoAnterior
                .multiply(multaAtraso)
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);

        return juros.add(multa).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Soma pagamentos de um cartao para a competencia informada.
     *
     * @param pagamentos  pagamentos do cartao
     * @param competencia competencia de referencia
     * @return total pago no mes
     */
    private BigDecimal somarPagamentosMes(List<Pagamento> pagamentos, YearMonth competencia) {
        return pagamentos.stream()
                .filter(pagamento -> YearMonth.from(pagamento.getDataPagamento()).equals(competencia))
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Soma valores monetarios garantindo escala padrao de duas casas decimais.
     *
     * @param valores lista de valores
     * @return soma total
     */
    private BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Soma somente itens de um tipo especifico para os totais analiticos por categoria.
     *
     * @param itens itens detalhados
     * @param tipo  tipo alvo
     * @return total do tipo solicitado
     */
    private BigDecimal somarPorTipo(List<ItemRelatorioDto> itens, TipoLancamento tipo) {
        return itens.stream()
                .filter(item -> item.tipoLancamento() == tipo)
                .map(ItemRelatorioDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula valor de parcela com ajuste da ultima parcela para fechar centavos.
     *
     * @param valorTotal         valor total da compra
     * @param quantidadeParcelas quantidade de parcelas
     * @param numeroParcela      parcela atual (1-indexada)
     * @return valor da parcela atual
     */
    private BigDecimal calcularValorParcela(BigDecimal valorTotal, int quantidadeParcelas, int numeroParcela) {
        BigDecimal base = valorTotal.divide(BigDecimal.valueOf(quantidadeParcelas), 2, RoundingMode.HALF_UP);

        if (numeroParcela < quantidadeParcelas) {
            return base;
        }

        BigDecimal somaAnteriores = base.multiply(BigDecimal.valueOf(quantidadeParcelas - 1));
        return valorTotal.subtract(somaAnteriores).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Mascara numero de cartao para exibir apenas os 4 primeiros e 4 ultimos digitos.
     *
     * @param numero numero completo sem mascara
     * @return numero parcialmente mascarado
     */
    private String mascararParcialCartao(String numero) {
        if (numero == null || numero.length() != 16) {
            return numero;
        }
        return numero.substring(0, 4) + " **** **** " + numero.substring(12);
    }
}
