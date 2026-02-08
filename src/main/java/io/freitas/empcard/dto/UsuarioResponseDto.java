package io.freitas.empcard.dto;

import io.freitas.empcard.model.PapelUsuario;

/**
 * Saida padronizada de usuario na API REST.
 */
public record UsuarioResponseDto(
        Long id,
        String nomeExibicao,
        String nomeUsuario,
        PapelUsuario papel,
        boolean ativo
) {
}
