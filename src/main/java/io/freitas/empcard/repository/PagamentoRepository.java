package io.freitas.empcard.repository;

import io.freitas.empcard.model.Pagamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<Pagamento> findAllByOrderByDataPagamentoDescIdDesc();

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<Pagamento> findByPessoaIdAndCartaoIdOrderByDataPagamentoAsc(Long pessoaId, Long cartaoId);

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<Pagamento> findByPessoaIdOrderByDataPagamentoAsc(Long pessoaId);

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByCartaoId(Long cartaoId);
}
