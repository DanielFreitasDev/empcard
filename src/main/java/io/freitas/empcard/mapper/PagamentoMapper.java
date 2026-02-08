package io.freitas.empcard.mapper;

import io.freitas.empcard.dto.PagamentoResponseDto;
import io.freitas.empcard.model.Pagamento;

/**
 * Mapper manual para converter Pagamento em DTO de resposta.
 */
public final class PagamentoMapper {

    private PagamentoMapper() {
    }

    /**
     * Converte entidade Pagamento para DTO REST.
     *
     * @param pagamento entidade origem
     * @return DTO de resposta
     */
    public static PagamentoResponseDto paraResponse(Pagamento pagamento) {
        return new PagamentoResponseDto(
                pagamento.getId(),
                pagamento.getPessoa().getId(),
                pagamento.getPessoa().getNome(),
                pagamento.getCartao().getId(),
                pagamento.getCartao().getNumero(),
                pagamento.getDataPagamento(),
                pagamento.getValor(),
                pagamento.getObservacao()
        );
    }
}
