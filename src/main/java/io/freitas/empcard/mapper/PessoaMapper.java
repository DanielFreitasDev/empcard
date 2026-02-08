package io.freitas.empcard.mapper;

import io.freitas.empcard.dto.PessoaResponseDto;
import io.freitas.empcard.model.Pessoa;

/**
 * Mapper manual para converter Pessoa em DTO de resposta.
 */
public final class PessoaMapper {

    private PessoaMapper() {
    }

    /**
     * Converte entidade Pessoa para DTO de resposta REST.
     *
     * @param pessoa entidade origem
     * @return DTO de saida
     */
    public static PessoaResponseDto paraResponse(Pessoa pessoa) {
        return new PessoaResponseDto(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getLogradouro(),
                pessoa.getNumero(),
                pessoa.getComplemento(),
                pessoa.getReferencia(),
                pessoa.getBairro(),
                pessoa.getCidade(),
                pessoa.getEstado(),
                pessoa.getCep(),
                pessoa.getCelular(),
                pessoa.getWhatsapp(),
                pessoa.getEmail(),
                pessoa.getJurosMensal(),
                pessoa.getMultaAtraso(),
                pessoa.isAtivo()
        );
    }
}
