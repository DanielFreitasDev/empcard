package io.freitas.empcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Dados de entrada para cadastro e edicao de pagamentos.
 */
@Getter
@Setter
public class PagamentoFormDto {

    @NotNull(message = "Pessoa e obrigatoria")
    private Long pessoaId;

    @NotNull(message = "Cartao e obrigatorio")
    private Long cartaoId;

    @NotNull(message = "Data do pagamento e obrigatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPagamento;

    @NotBlank(message = "Valor e obrigatorio")
    private String valorFormatado;

    @Size(max = 255, message = "Observacao deve ter no maximo 255 caracteres")
    private String observacao;
}
