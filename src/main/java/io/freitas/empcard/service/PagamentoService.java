package io.freitas.empcard.service;

import io.freitas.empcard.dto.PagamentoFormDto;
import io.freitas.empcard.exception.RecursoNaoEncontradoException;
import io.freitas.empcard.exception.RegraDeNegocioException;
import io.freitas.empcard.model.Cartao;
import io.freitas.empcard.model.Pagamento;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.repository.PagamentoRepository;
import io.freitas.empcard.util.TextoUtils;
import io.freitas.empcard.util.ValorMonetarioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regras de negocio para pagamentos recebidos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;

    /**
     * Lista pagamentos para consulta geral ordenada por data mais recente.
     *
     * @return lista de pagamentos
     */
    @Transactional(readOnly = true)
    public List<Pagamento> listarTodos() {
        log.info("Listando todos os pagamentos");
        return pagamentoRepository.findAllByOrderByDataPagamentoDescIdDesc();
    }

    /**
     * Busca pagamento por id com validacao de existencia.
     *
     * @param id identificador do pagamento
     * @return pagamento encontrado
     */
    @Transactional(readOnly = true)
    public Pagamento buscarPorId(Long id) {
        log.info("Buscando pagamento por id={}", id);
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento nao encontrado para id " + id));
    }

    /**
     * Cria pagamento validando valor monetario e vinculacoes de pessoa/cartao.
     *
     * @param form dados do formulario
     * @return pagamento salvo
     */
    @Transactional
    public Pagamento criar(PagamentoFormDto form) {
        Pessoa pessoa = pessoaService.buscarPorId(form.getPessoaId());
        Cartao cartao = cartaoService.buscarPorId(form.getCartaoId());

        BigDecimal valor = ValorMonetarioUtils.parse(form.getValorFormatado());
        if (valor.signum() <= 0) {
            throw new RegraDeNegocioException("Valor do pagamento deve ser maior que zero");
        }

        Pagamento pagamento = new Pagamento();
        aplicarDados(form, pagamento, pessoa, cartao, valor);

        Pagamento salvo = pagamentoRepository.save(pagamento);
        log.info("Pagamento criado com sucesso. id={}, pessoaId={}, cartaoId={}", salvo.getId(), pessoa.getId(), cartao.getId());
        return salvo;
    }

    /**
     * Atualiza pagamento existente preservando consistencia dos dados financeiros.
     *
     * @param id   identificador do pagamento
     * @param form dados atualizados
     * @return pagamento atualizado
     */
    @Transactional
    public Pagamento atualizar(Long id, PagamentoFormDto form) {
        Pagamento pagamento = buscarPorId(id);
        Pessoa pessoa = pessoaService.buscarPorId(form.getPessoaId());
        Cartao cartao = cartaoService.buscarPorId(form.getCartaoId());

        BigDecimal valor = ValorMonetarioUtils.parse(form.getValorFormatado());
        if (valor.signum() <= 0) {
            throw new RegraDeNegocioException("Valor do pagamento deve ser maior que zero");
        }

        aplicarDados(form, pagamento, pessoa, cartao, valor);

        Pagamento salvo = pagamentoRepository.save(pagamento);
        log.info("Pagamento atualizado com sucesso. id={}", salvo.getId());
        return salvo;
    }

    /**
     * Exclui pagamento quando houver necessidade de correcao administrativa.
     *
     * @param id identificador do pagamento
     */
    @Transactional
    public void excluir(Long id) {
        Pagamento pagamento = buscarPorId(id);
        pagamentoRepository.delete(pagamento);
        log.info("Pagamento excluido com sucesso. id={}", id);
    }

    /**
     * Converte entidade para DTO de formulario de edicao.
     *
     * @param pagamento entidade de origem
     * @return dto preenchido
     */
    @Transactional(readOnly = true)
    public PagamentoFormDto paraForm(Pagamento pagamento) {
        PagamentoFormDto form = new PagamentoFormDto();
        form.setPessoaId(pagamento.getPessoa().getId());
        form.setCartaoId(pagamento.getCartao().getId());
        form.setDataPagamento(pagamento.getDataPagamento());
        form.setValorFormatado(ValorMonetarioUtils.formatar(pagamento.getValor()));
        form.setObservacao(pagamento.getObservacao());
        return form;
    }

    /**
     * Copia dados normalizados para entidade de pagamento.
     *
     * @param form      formulario de entrada
     * @param pagamento entidade destino
     * @param pessoa    pessoa relacionada
     * @param cartao    cartao relacionado
     * @param valor     valor ja convertido
     */
    private void aplicarDados(PagamentoFormDto form, Pagamento pagamento, Pessoa pessoa, Cartao cartao, BigDecimal valor) {
        pagamento.setPessoa(pessoa);
        pagamento.setCartao(cartao);
        pagamento.setDataPagamento(form.getDataPagamento());
        pagamento.setValor(valor);
        pagamento.setObservacao(TextoUtils.normalizarMaiusculo(form.getObservacao()));
    }
}
