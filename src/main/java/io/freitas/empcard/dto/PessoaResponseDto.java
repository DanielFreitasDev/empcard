package io.freitas.empcard.dto;

import java.math.BigDecimal;

/**
 * Saida padronizada de pessoa na API REST.
 */
public record PessoaResponseDto(
        Long id,
        String nome,
        String cpf,
        String logradouro,
        String numero,
        String complemento,
        String referencia,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String celular,
        String whatsapp,
        String email,
        BigDecimal jurosMensal,
        BigDecimal multaAtraso,
        boolean ativo
) {
}
