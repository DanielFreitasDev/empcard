package io.freitas.empcard.dto;

import io.freitas.empcard.model.TipoLancamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Dados de entrada para cadastro e edicao de lancamentos de compra.
 */
@Getter
@Setter
public class LancamentoFormDto {

    @NotNull(message = "Pessoa e obrigatoria")
    private Long pessoaId;

    @NotNull(message = "Cartao e obrigatorio")
    private Long cartaoId;

    @NotBlank(message = "Descricao e obrigatoria")
    @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
    private String descricao;

    @NotNull(message = "Tipo de lancamento e obrigatorio")
    private TipoLancamento tipo;

    @NotBlank(message = "Valor e obrigatorio")
    private String valorFormatado;

    @NotNull(message = "Quantidade de parcelas e obrigatoria")
    @Min(value = 1, message = "Quantidade de parcelas deve ser no minimo 1")
    @Max(value = 360, message = "Quantidade de parcelas deve ser no maximo 360")
    private Integer quantidadeParcelas;

    @NotNull(message = "Data da compra e obrigatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataCompra;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimFixo;

    @Size(max = 255, message = "Observacao deve ter no maximo 255 caracteres")
    private String observacao;

    private boolean ativo = true;
}
