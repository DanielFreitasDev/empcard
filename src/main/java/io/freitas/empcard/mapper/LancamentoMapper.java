package io.freitas.empcard.mapper;

import io.freitas.empcard.dto.LancamentoResponseDto;
import io.freitas.empcard.model.Lancamento;

/**
 * Mapper manual para converter Lancamento em DTO de resposta.
 */
public final class LancamentoMapper {

    private LancamentoMapper() {
    }

    /**
     * Converte entidade Lancamento para DTO REST.
     *
     * @param lancamento entidade origem
     * @return DTO de resposta
     */
    public static LancamentoResponseDto paraResponse(Lancamento lancamento) {
        return new LancamentoResponseDto(
                lancamento.getId(),
                lancamento.getPessoa().getId(),
                lancamento.getPessoa().getNome(),
                lancamento.getCartao().getId(),
                lancamento.getCartao().getNumero(),
                lancamento.getDescricao(),
                lancamento.getTipo(),
                lancamento.getValorTotal(),
                lancamento.getQuantidadeParcelas(),
                lancamento.getDataCompra(),
                lancamento.getDataFimFixo(),
                lancamento.getObservacao(),
                lancamento.isAtivo()
        );
    }
}
