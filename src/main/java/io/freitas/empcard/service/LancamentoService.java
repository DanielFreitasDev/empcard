package io.freitas.empcard.service;

import io.freitas.empcard.dto.LancamentoFormDto;
import io.freitas.empcard.exception.RecursoNaoEncontradoException;
import io.freitas.empcard.exception.RegraDeNegocioException;
import io.freitas.empcard.model.Cartao;
import io.freitas.empcard.model.Lancamento;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.model.TipoLancamento;
import io.freitas.empcard.repository.LancamentoRepository;
import io.freitas.empcard.util.TextoUtils;
import io.freitas.empcard.util.ValorMonetarioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servico de regras para lancamentos de compras em cartao.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;

    /**
     * Lista lancamentos com pessoa e cartao carregados para evitar consultas repetidas na tela.
     *
     * @return lista de lancamentos em ordem decrescente de data
     */
    @Transactional(readOnly = true)
    public List<Lancamento> listarTodos() {
        log.info("Listando todos os lancamentos");
        return lancamentoRepository.findAllByOrderByDataCompraDescIdDesc();
    }

    /**
     * Busca um lancamento por id validando existencia.
     *
     * @param id identificador do lancamento
     * @return lancamento encontrado
     */
    @Transactional(readOnly = true)
    public Lancamento buscarPorId(Long id) {
        log.info("Buscando lancamento por id={}", id);
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento nao encontrado para id " + id));
    }

    /**
     * Cria novo lancamento com validacoes de tipo, parcelas e valor monetario.
     *
     * @param form dados do formulario
     * @return lancamento salvo
     */
    @Transactional
    public Lancamento criar(LancamentoFormDto form) {
        Pessoa pessoa = pessoaService.buscarPorId(form.getPessoaId());
        Cartao cartao = cartaoService.buscarPorId(form.getCartaoId());

        validarRegrasTipo(form);

        Lancamento lancamento = new Lancamento();
        aplicarDados(form, lancamento, pessoa, cartao);

        Lancamento salvo = lancamentoRepository.save(lancamento);
        log.info("Lancamento criado com sucesso. id={}, tipo={}", salvo.getId(), salvo.getTipo());
        return salvo;
    }

    /**
     * Atualiza lancamento existente garantindo regras coerentes do tipo de compra.
     *
     * @param id   identificador do lancamento
     * @param form dados atualizados
     * @return lancamento atualizado
     */
    @Transactional
    public Lancamento atualizar(Long id, LancamentoFormDto form) {
        Lancamento lancamento = buscarPorId(id);
        Pessoa pessoa = pessoaService.buscarPorId(form.getPessoaId());
        Cartao cartao = cartaoService.buscarPorId(form.getCartaoId());

        validarRegrasTipo(form);
        aplicarDados(form, lancamento, pessoa, cartao);

        Lancamento salvo = lancamentoRepository.save(lancamento);
        log.info("Lancamento atualizado com sucesso. id={}", salvo.getId());
        return salvo;
    }

    /**
     * Alterna status ativo para bloquear novas cobrancas futuras sem excluir historico.
     *
     * @param id identificador do lancamento
     */
    @Transactional
    public void alternarAtivo(Long id) {
        Lancamento lancamento = buscarPorId(id);
        lancamento.setAtivo(!lancamento.isAtivo());
        lancamentoRepository.save(lancamento);
        log.info("Status do lancamento alterado. id={}, ativo={}", lancamento.getId(), lancamento.isAtivo());
    }

    /**
     * Exclui lancamento definitivamente.
     *
     * @param id identificador do lancamento
     */
    @Transactional
    public void excluir(Long id) {
        Lancamento lancamento = buscarPorId(id);
        lancamentoRepository.delete(lancamento);
        log.info("Lancamento excluido com sucesso. id={}", id);
    }

    /**
     * Mapeia entidade para formulario de edicao.
     *
     * @param lancamento entidade de origem
     * @return dto preenchido
     */
    @Transactional(readOnly = true)
    public LancamentoFormDto paraForm(Lancamento lancamento) {
        LancamentoFormDto form = new LancamentoFormDto();
        form.setPessoaId(lancamento.getPessoa().getId());
        form.setCartaoId(lancamento.getCartao().getId());
        form.setDescricao(lancamento.getDescricao());
        form.setTipo(lancamento.getTipo());
        form.setValorFormatado(ValorMonetarioUtils.formatar(lancamento.getValorTotal()));
        form.setQuantidadeParcelas(lancamento.getQuantidadeParcelas());
        form.setDataCompra(lancamento.getDataCompra());
        form.setDataFimFixo(lancamento.getDataFimFixo());
        form.setObservacao(lancamento.getObservacao());
        form.setAtivo(lancamento.isAtivo());
        return form;
    }

    /**
     * Valida restricoes de negocio por tipo de lancamento para evitar dados inconsistentes.
     *
     * @param form formulario recebido
     */
    private void validarRegrasTipo(LancamentoFormDto form) {
        if (form.getTipo() == TipoLancamento.PARCELADO && form.getQuantidadeParcelas() <= 1) {
            throw new RegraDeNegocioException("Lancamento parcelado deve ter ao menos 2 parcelas");
        }

        if (form.getTipo() != TipoLancamento.PARCELADO && form.getQuantidadeParcelas() != 1) {
            throw new RegraDeNegocioException("Lancamento nao parcelado deve possuir exatamente 1 parcela");
        }

        if (form.getTipo() != TipoLancamento.FIXO && form.getDataFimFixo() != null) {
            throw new RegraDeNegocioException("Data fim fixo so pode ser usada em lancamento FIXO");
        }

        if (form.getTipo() == TipoLancamento.FIXO
                && form.getDataFimFixo() != null
                && form.getDataFimFixo().isBefore(form.getDataCompra())) {
            throw new RegraDeNegocioException("Data fim fixo nao pode ser anterior a data da compra");
        }

        BigDecimal valor = ValorMonetarioUtils.parse(form.getValorFormatado());
        if (valor.signum() <= 0) {
            throw new RegraDeNegocioException("Valor do lancamento deve ser maior que zero");
        }
    }

    /**
     * Copia valores do formulario para entidade com normalizacao e parse monetario.
     *
     * @param form       dados de entrada
     * @param lancamento entidade destino
     * @param pessoa     pessoa relacionada
     * @param cartao     cartao relacionado
     */
    private void aplicarDados(LancamentoFormDto form, Lancamento lancamento, Pessoa pessoa, Cartao cartao) {
        lancamento.setPessoa(pessoa);
        lancamento.setCartao(cartao);
        lancamento.setDescricao(TextoUtils.normalizarMaiusculo(form.getDescricao()));
        lancamento.setTipo(form.getTipo());
        lancamento.setValorTotal(ValorMonetarioUtils.parse(form.getValorFormatado()));
        lancamento.setQuantidadeParcelas(form.getQuantidadeParcelas());
        lancamento.setDataCompra(form.getDataCompra());
        lancamento.setDataFimFixo(form.getDataFimFixo());
        lancamento.setObservacao(TextoUtils.normalizarMaiusculo(form.getObservacao()));
        lancamento.setAtivo(form.isAtivo());
    }
}
