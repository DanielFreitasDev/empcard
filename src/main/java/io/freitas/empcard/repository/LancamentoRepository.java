package io.freitas.empcard.repository;

import io.freitas.empcard.model.Lancamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<Lancamento> findAllByOrderByDataCompraDescIdDesc();

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<Lancamento> findByPessoaIdOrderByDataCompraAsc(Long pessoaId);

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<Lancamento> findByPessoaIdAndCartaoIdOrderByDataCompraAsc(Long pessoaId, Long cartaoId);

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByCartaoId(Long cartaoId);
}
