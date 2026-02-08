package io.freitas.empcard.service;

import io.freitas.empcard.dto.PessoaFormDto;
import io.freitas.empcard.exception.RecursoNaoEncontradoException;
import io.freitas.empcard.exception.RegraDeNegocioException;
import io.freitas.empcard.model.Pessoa;
import io.freitas.empcard.repository.LancamentoRepository;
import io.freitas.empcard.repository.PagamentoRepository;
import io.freitas.empcard.repository.PessoaRepository;
import io.freitas.empcard.util.DocumentoUtils;
import io.freitas.empcard.util.TextoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Regras de negocio para cadastro e manutencao de pessoas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final PagamentoRepository pagamentoRepository;

    /**
     * Lista todas as pessoas ordenadas por nome para facilitar a navegacao no cadastro.
     *
     * @return lista completa de pessoas
     */
    @Transactional(readOnly = true)
    public List<Pessoa> listarTodos() {
        log.info("Listando todas as pessoas cadastradas");
        return pessoaRepository.findAll().stream()
                .sorted(Comparator.comparing(Pessoa::getNome, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * Lista apenas pessoas ativas para uso em combos de cadastro de lancamentos e pagamentos.
     *
     * @return pessoas ativas ordenadas por nome
     */
    @Transactional(readOnly = true)
    public List<Pessoa> listarAtivos() {
        log.info("Listando pessoas ativas");
        return pessoaRepository.findAll().stream()
                .filter(Pessoa::isAtivo)
                .sorted(Comparator.comparing(Pessoa::getNome, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * Busca pessoa pelo identificador e falha rapidamente quando nao existir.
     *
     * @param id identificador da pessoa
     * @return pessoa encontrada
     */
    @Transactional(readOnly = true)
    public Pessoa buscarPorId(Long id) {
        log.info("Buscando pessoa por id={}", id);
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa nao encontrada para id " + id));
    }

    /**
     * Cria uma nova pessoa validando unicidade de CPF e normalizando dados para uppercase.
     *
     * @param form dados de cadastro
     * @return pessoa persistida
     */
    @Transactional
    public Pessoa criar(PessoaFormDto form) {
        String cpf = DocumentoUtils.somenteDigitos(form.getCpf());
        validarCpfUnico(cpf, null);

        Pessoa pessoa = new Pessoa();
        aplicarDados(form, pessoa, cpf);

        Pessoa salvo = pessoaRepository.save(pessoa);
        log.info("Pessoa criada com sucesso. id={}, cpf={}", salvo.getId(), salvo.getCpf());
        return salvo;
    }

    /**
     * Atualiza cadastro de pessoa existente preservando a integridade das regras de validacao.
     *
     * @param id   identificador da pessoa
     * @param form dados atualizados
     * @return pessoa atualizada
     */
    @Transactional
    public Pessoa atualizar(Long id, PessoaFormDto form) {
        Pessoa pessoa = buscarPorId(id);
        String cpf = DocumentoUtils.somenteDigitos(form.getCpf());
        validarCpfUnico(cpf, id);

        aplicarDados(form, pessoa, cpf);

        Pessoa salvo = pessoaRepository.save(pessoa);
        log.info("Pessoa atualizada com sucesso. id={}", salvo.getId());
        return salvo;
    }

    /**
     * Alterna o status ativo/inativo da pessoa sem remover historico financeiro.
     *
     * @param id identificador da pessoa
     */
    @Transactional
    public void alternarAtivo(Long id) {
        Pessoa pessoa = buscarPorId(id);
        pessoa.setAtivo(!pessoa.isAtivo());
        pessoaRepository.save(pessoa);
        log.info("Status da pessoa alterado. id={}, ativo={}", pessoa.getId(), pessoa.isAtivo());
    }

    /**
     * Remove pessoa somente quando nao houver vinculos em lancamentos ou pagamentos.
     *
     * @param id identificador da pessoa
     */
    @Transactional
    public void excluir(Long id) {
        Pessoa pessoa = buscarPorId(id);

        if (lancamentoRepository.existsByPessoaId(id) || pagamentoRepository.existsByPessoaId(id)) {
            log.warn("Tentativa de excluir pessoa com vinculacoes. id={}", id);
            throw new RegraDeNegocioException("Nao e permitido excluir pessoa com lancamentos ou pagamentos associados");
        }

        pessoaRepository.delete(pessoa);
        log.info("Pessoa excluida com sucesso. id={}", id);
    }

    /**
     * Converte entidade em formulario para preencher tela de edicao.
     *
     * @param pessoa entidade de origem
     * @return dto de formulario
     */
    @Transactional(readOnly = true)
    public PessoaFormDto paraForm(Pessoa pessoa) {
        PessoaFormDto form = new PessoaFormDto();
        form.setNome(pessoa.getNome());
        form.setCpf(pessoa.getCpf());
        form.setLogradouro(pessoa.getLogradouro());
        form.setNumero(pessoa.getNumero());
        form.setComplemento(pessoa.getComplemento());
        form.setReferencia(pessoa.getReferencia());
        form.setBairro(pessoa.getBairro());
        form.setCidade(pessoa.getCidade());
        form.setEstado(pessoa.getEstado());
        form.setCep(pessoa.getCep());
        form.setCelular(pessoa.getCelular());
        form.setWhatsapp(pessoa.getWhatsapp());
        form.setEmail(pessoa.getEmail());
        form.setJurosMensal(pessoa.getJurosMensal());
        form.setMultaAtraso(pessoa.getMultaAtraso());
        form.setAtivo(pessoa.isAtivo());
        return form;
    }

    /**
     * Valida duplicidade de CPF no cadastro de pessoas.
     *
     * @param cpf     cpf somente com digitos
     * @param idAtual id da pessoa em edicao (null para criacao)
     */
    private void validarCpfUnico(String cpf, Long idAtual) {
        boolean duplicado = idAtual == null
                ? pessoaRepository.existsByCpf(cpf)
                : pessoaRepository.existsByCpfAndIdNot(cpf, idAtual);

        if (duplicado) {
            log.warn("CPF ja cadastrado: {}", cpf);
            throw new RegraDeNegocioException("Ja existe pessoa cadastrada com este CPF");
        }
    }

    /**
     * Aplica transformacoes de negocio e normalizacao de texto antes de persistir.
     *
     * @param form   dados recebidos do formulario
     * @param pessoa entidade destino
     * @param cpf    cpf sem mascara ja higienizado
     */
    private void aplicarDados(PessoaFormDto form, Pessoa pessoa, String cpf) {
        // Campos textuais gerais ficam em uppercase para manter padrao visual e de busca.
        pessoa.setNome(TextoUtils.normalizarMaiusculo(form.getNome()));
        pessoa.setCpf(cpf);
        pessoa.setLogradouro(TextoUtils.normalizarMaiusculo(form.getLogradouro()));
        pessoa.setNumero(TextoUtils.normalizarMaiusculo(form.getNumero()));
        pessoa.setComplemento(TextoUtils.normalizarMaiusculo(form.getComplemento()));
        pessoa.setReferencia(TextoUtils.normalizarMaiusculo(form.getReferencia()));
        pessoa.setBairro(TextoUtils.normalizarMaiusculo(form.getBairro()));
        pessoa.setCidade(TextoUtils.normalizarMaiusculo(form.getCidade()));
        pessoa.setEstado(TextoUtils.normalizarMaiusculo(form.getEstado()));

        // Campos numericos guardam apenas digitos para simplificar mascaras no frontend.
        pessoa.setCep(DocumentoUtils.somenteDigitos(form.getCep()));
        pessoa.setCelular(DocumentoUtils.somenteDigitos(form.getCelular()));
        pessoa.setWhatsapp(DocumentoUtils.somenteDigitos(form.getWhatsapp()));

        // E-mail preserva caixa original para respeitar preferencia do usuario.
        pessoa.setEmail(TextoUtils.normalizarSimples(form.getEmail()));
        pessoa.setJurosMensal(form.getJurosMensal() == null ? BigDecimal.ZERO : form.getJurosMensal());
        pessoa.setMultaAtraso(form.getMultaAtraso() == null ? BigDecimal.ZERO : form.getMultaAtraso());
        pessoa.setAtivo(form.isAtivo());
    }
}
