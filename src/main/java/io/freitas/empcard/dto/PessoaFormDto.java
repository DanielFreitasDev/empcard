package io.freitas.empcard.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Dados de entrada para cadastro e edicao de pessoas.
 */
@Getter
@Setter
public class PessoaFormDto {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 160, message = "Nome deve ter no maximo 160 caracteres")
    private String nome;

    @NotBlank(message = "CPF e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 digitos")
    private String cpf;

    @Size(max = 160, message = "Logradouro deve ter no maximo 160 caracteres")
    private String logradouro;

    @Size(max = 20, message = "Numero deve ter no maximo 20 caracteres")
    private String numero;

    @Size(max = 120, message = "Complemento deve ter no maximo 120 caracteres")
    private String complemento;

    @Size(max = 120, message = "Referencia deve ter no maximo 120 caracteres")
    private String referencia;

    @Size(max = 120, message = "Bairro deve ter no maximo 120 caracteres")
    private String bairro;

    @Size(max = 120, message = "Cidade deve ter no maximo 120 caracteres")
    private String cidade;

    @Pattern(regexp = "^$|[A-Z]{2}", message = "Estado deve conter 2 letras")
    private String estado;

    @Pattern(regexp = "^$|\\d{8}", message = "CEP deve conter 8 digitos")
    private String cep;

    @Pattern(regexp = "^$|\\d{10,11}", message = "Celular deve conter 10 ou 11 digitos")
    private String celular;

    @Pattern(regexp = "^$|\\d{10,11}", message = "WhatsApp deve conter 10 ou 11 digitos")
    private String whatsapp;

    @Email(message = "E-mail invalido")
    @Size(max = 160, message = "E-mail deve ter no maximo 160 caracteres")
    private String email;

    @DecimalMin(value = "0.0000", message = "Juros mensal nao pode ser negativo")
    @DecimalMax(value = "999.9999", message = "Juros mensal excede limite")
    private BigDecimal jurosMensal = BigDecimal.ZERO;

    @DecimalMin(value = "0.0000", message = "Multa de atraso nao pode ser negativa")
    @DecimalMax(value = "999.9999", message = "Multa de atraso excede limite")
    private BigDecimal multaAtraso = BigDecimal.ZERO;

    private boolean ativo = true;
}
