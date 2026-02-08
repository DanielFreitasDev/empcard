package io.freitas.empcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dados para redefinicao administrativa de senha de usuario.
 */
@Getter
@Setter
public class ResetSenhaUsuarioDto {

    @NotBlank(message = "Nova senha e obrigatoria")
    @Size(min = 6, max = 120, message = "Nova senha deve ter entre 6 e 120 caracteres")
    private String novaSenha;
}
