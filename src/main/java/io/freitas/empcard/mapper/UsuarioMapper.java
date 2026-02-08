package io.freitas.empcard.mapper;

import io.freitas.empcard.dto.UsuarioResponseDto;
import io.freitas.empcard.model.Usuario;

/**
 * Mapper manual para converter Usuario em DTO de resposta.
 */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    /**
     * Converte entidade Usuario para DTO REST.
     *
     * @param usuario entidade origem
     * @return DTO de saida
     */
    public static UsuarioResponseDto paraResponse(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNomeExibicao(),
                usuario.getNomeUsuario(),
                usuario.getPapel(),
                usuario.isAtivo()
        );
    }
}
