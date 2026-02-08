package io.freitas.empcard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dados de entrada para cadastro e edicao de cartoes.
 */
@Getter
@Setter
public class CartaoFormDto {

    @NotBlank(message = "Numero do cartao e obrigatorio")
    @Pattern(regexp = "\\d{16}", message = "Numero do cartao deve conter exatamente 16 digitos")
    private String numero;

    @NotBlank(message = "Bandeira e obrigatoria")
    @Size(max = 40, message = "Bandeira deve ter no maximo 40 caracteres")
    private String bandeira;

    @NotBlank(message = "Banco e obrigatorio")
    @Size(max = 120, message = "Banco deve ter no maximo 120 caracteres")
    private String banco;

    @NotNull(message = "Dia de fechamento e obrigatorio")
    @Min(value = 1, message = "Dia de fechamento minimo e 1")
    @Max(value = 31, message = "Dia de fechamento maximo e 31")
    private Integer diaFechamento;

    @NotNull(message = "Dia de vencimento e obrigatorio")
    @Min(value = 1, message = "Dia de vencimento minimo e 1")
    @Max(value = 31, message = "Dia de vencimento maximo e 31")
    private Integer diaVencimento;

    private boolean ativo = true;
}
