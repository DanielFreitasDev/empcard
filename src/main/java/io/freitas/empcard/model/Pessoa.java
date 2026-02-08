package io.freitas.empcard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Cadastro de pessoa que utiliza os cartoes emprestados e pode ter cobrancas pendentes.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pessoas")
public class Pessoa extends EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(length = 160)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 120)
    private String complemento;

    @Column(length = 120)
    private String referencia;

    @Column(length = 120)
    private String bairro;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 8)
    private String cep;

    @Column(length = 11)
    private String celular;

    @Column(length = 11)
    private String whatsapp;

    @Column(length = 160)
    private String email;

    @Column(name = "juros_mensal", nullable = false, precision = 7, scale = 4)
    private BigDecimal jurosMensal = BigDecimal.ZERO;

    @Column(name = "multa_atraso", nullable = false, precision = 7, scale = 4)
    private BigDecimal multaAtraso = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean ativo = true;
}
