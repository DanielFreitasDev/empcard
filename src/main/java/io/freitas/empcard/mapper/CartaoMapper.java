package io.freitas.empcard.mapper;

import io.freitas.empcard.dto.CartaoResponseDto;
import io.freitas.empcard.model.Cartao;

/**
 * Mapper manual para converter Cartao em DTO de resposta.
 */
public final class CartaoMapper {

    private CartaoMapper() {
    }

    /**
     * Converte entidade Cartao para DTO de saida REST.
     *
     * @param cartao entidade origem
     * @return DTO de resposta
     */
    public static CartaoResponseDto paraResponse(Cartao cartao) {
        return new CartaoResponseDto(
                cartao.getId(),
                cartao.getNumero(),
                cartao.getBandeira(),
                cartao.getBanco(),
                cartao.getDiaFechamento() == null ? null : cartao.getDiaFechamento().intValue(),
                cartao.getDiaVencimento() == null ? null : cartao.getDiaVencimento().intValue(),
                cartao.isAtivo()
        );
    }
}
