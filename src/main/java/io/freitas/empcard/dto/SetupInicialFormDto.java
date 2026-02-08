package io.freitas.empcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dados de entrada para criacao do primeiro usuario administrador.
 */
@Getter
@Setter
public class SetupInicialFormDto {

    @NotBlank(message = "Nome de exibicao e obrigatorio")
    @Size(max = 120, message = "Nome de exibicao deve ter no maximo 120 caracteres")
    private String nomeExibicao;

    @NotBlank(message = "Nome de usuario e obrigatorio")
    @Size(max = 80, message = "Nome de usuario deve ter no maximo 80 caracteres")
    private String nomeUsuario;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
    private String senha;
}
