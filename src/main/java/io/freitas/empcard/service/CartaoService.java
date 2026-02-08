package io.freitas.empcard.service;

import io.freitas.empcard.dto.CartaoFormDto;
import io.freitas.empcard.exception.RecursoNaoEncontradoException;
import io.freitas.empcard.exception.RegraDeNegocioException;
import io.freitas.empcard.model.Cartao;
import io.freitas.empcard.repository.CartaoRepository;
import io.freitas.empcard.repository.LancamentoRepository;
import io.freitas.empcard.repository.PagamentoRepository;
import io.freitas.empcard.util.DocumentoUtils;
import io.freitas.empcard.util.TextoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Regras de negocio para cadastro e manutencao de cartoes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final PagamentoRepository pagamentoRepository;

    /**
     * Lista todos os cartoes ordenados por banco e numero.
     *
     * @return lista completa de cartoes
     */
    @Transactional(readOnly = true)
    public List<Cartao> listarTodos() {
        log.info("Listando todos os cartoes");
        return cartaoRepository.findAll().stream()
                .sorted(Comparator.comparing(Cartao::getBanco, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Cartao::getNumero, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * Lista cartoes ativos para uso em formularios operacionais.
     *
     * @return cartoes ativos
     */
    @Transactional(readOnly = true)
    public List<Cartao> listarAtivos() {
        log.info("Listando cartoes ativos");
        return cartaoRepository.findAll().stream()
                .filter(Cartao::isAtivo)
                .sorted(Comparator.comparing(Cartao::getBanco, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * Busca cartao por id com validacao de existencia.
     *
     * @param id identificador do cartao
     * @return entidade cartao
     */
    @Transactional(readOnly = true)
    public Cartao buscarPorId(Long id) {
        log.info("Buscando cartao por id={}", id);
        return cartaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cartao nao encontrado para id " + id));
    }

    /**
     * Cria novo cartao com validacao de numero unico e regras de fechamento/vencimento.
     *
     * @param form dados do formulario
     * @return cartao persistido
     */
    @Transactional
    public Cartao criar(CartaoFormDto form) {
        String numero = DocumentoUtils.somenteDigitos(form.getNumero());
        validarNumeroUnico(numero, null);

        Cartao cartao = new Cartao();
        aplicarDados(form, cartao, numero);

        Cartao salvo = cartaoRepository.save(cartao);
        log.info("Cartao criado com sucesso. id={}, numero={}", salvo.getId(), salvo.getNumero());
        return salvo;
    }

    /**
     * Atualiza cartao existente preservando regras de unicidade.
     *
     * @param id   identificador do cartao
     * @param form dados atualizados
     * @return cartao atualizado
     */
    @Transactional
    public Cartao atualizar(Long id, CartaoFormDto form) {
        Cartao cartao = buscarPorId(id);
        String numero = DocumentoUtils.somenteDigitos(form.getNumero());
        validarNumeroUnico(numero, id);

        aplicarDados(form, cartao, numero);

        Cartao salvo = cartaoRepository.save(cartao);
        log.info("Cartao atualizado com sucesso. id={}", salvo.getId());
        return salvo;
    }

    /**
     * Alterna status ativo do cartao para retiralo de novos cadastros sem perder historico.
     *
     * @param id identificador do cartao
     */
    @Transactional
    public void alternarAtivo(Long id) {
        Cartao cartao = buscarPorId(id);
        cartao.setAtivo(!cartao.isAtivo());
        cartaoRepository.save(cartao);
        log.info("Status do cartao alterado. id={}, ativo={}", cartao.getId(), cartao.isAtivo());
    }

    /**
     * Exclui cartao somente quando sem vinculos financeiros.
     *
     * @param id identificador do cartao
     */
    @Transactional
    public void excluir(Long id) {
        Cartao cartao = buscarPorId(id);

        if (lancamentoRepository.existsByCartaoId(id) || pagamentoRepository.existsByCartaoId(id)) {
            log.warn("Tentativa de excluir cartao com vinculacoes. id={}", id);
            throw new RegraDeNegocioException("Nao e permitido excluir cartao com lancamentos ou pagamentos associados");
        }

        cartaoRepository.delete(cartao);
        log.info("Cartao excluido com sucesso. id={}", id);
    }

    /**
     * Preenche DTO de formulario a partir da entidade para tela de edicao.
     *
     * @param cartao entidade de origem
     * @return formulario preenchido
     */
    @Transactional(readOnly = true)
    public CartaoFormDto paraForm(Cartao cartao) {
        CartaoFormDto form = new CartaoFormDto();
        form.setNumero(cartao.getNumero());
        form.setBandeira(cartao.getBandeira());
        form.setBanco(cartao.getBanco());
        form.setDiaFechamento(cartao.getDiaFechamento() == null ? null : cartao.getDiaFechamento().intValue());
        form.setDiaVencimento(cartao.getDiaVencimento() == null ? null : cartao.getDiaVencimento().intValue());
        form.setAtivo(cartao.isAtivo());
        return form;
    }

    /**
     * Valida se numero de cartao ja existe para impedir duplicidade.
     *
     * @param numero  numero apenas com digitos
     * @param idAtual id em edicao ou null na criacao
     */
    private void validarNumeroUnico(String numero, Long idAtual) {
        boolean duplicado = idAtual == null
                ? cartaoRepository.existsByNumero(numero)
                : cartaoRepository.existsByNumeroAndIdNot(numero, idAtual);

        if (duplicado) {
            log.warn("Numero de cartao duplicado detectado: {}", numero);
            throw new RegraDeNegocioException("Ja existe cartao cadastrado com este numero");
        }
    }

    /**
     * Aplica normalizacao de dados e regras de negocio na entidade.
     *
     * @param form   dados de entrada
     * @param cartao entidade destino
     * @param numero numero higienizado sem mascara
     */
    private void aplicarDados(CartaoFormDto form, Cartao cartao, String numero) {
        cartao.setNumero(numero);
        cartao.setBandeira(TextoUtils.normalizarMaiusculo(form.getBandeira()));
        cartao.setBanco(TextoUtils.normalizarMaiusculo(form.getBanco()));
        cartao.setDiaFechamento(form.getDiaFechamento().shortValue());
        cartao.setDiaVencimento(form.getDiaVencimento().shortValue());
        cartao.setAtivo(form.isAtivo());
    }
}
