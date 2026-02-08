package io.freitas.empcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dados para alteracao de senha do usuario autenticado.
 */
@Getter
@Setter
public class AlterarSenhaFormDto {

    @NotBlank(message = "Senha atual e obrigatoria")
    private String senhaAtual;

    @NotBlank(message = "Nova senha e obrigatoria")
    @Size(min = 6, max = 120, message = "Nova senha deve ter entre 6 e 120 caracteres")
    private String novaSenha;
}
