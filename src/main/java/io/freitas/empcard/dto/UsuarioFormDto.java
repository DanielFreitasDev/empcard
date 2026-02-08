package io.freitas.empcard.dto;

import io.freitas.empcard.model.PapelUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dados de entrada para criacao de usuario pela administracao.
 */
@Getter
@Setter
public class UsuarioFormDto {

    @NotBlank(message = "Nome de exibicao e obrigatorio")
    @Size(max = 120, message = "Nome de exibicao deve ter no maximo 120 caracteres")
    private String nomeExibicao;

    @NotBlank(message = "Nome de usuario e obrigatorio")
    @Size(max = 80, message = "Nome de usuario deve ter no maximo 80 caracteres")
    private String nomeUsuario;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
    private String senha;

    @NotNull(message = "Papel e obrigatorio")
    private PapelUsuario papel;

    private boolean ativo = true;
}
