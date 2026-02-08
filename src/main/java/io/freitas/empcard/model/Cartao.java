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

/**
 * Cadastro de cartao de credito com suas regras de fechamento e vencimento.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cartoes")
public class Cartao extends EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String numero;

    @Column(nullable = false, length = 40)
    private String bandeira;

    @Column(nullable = false, length = 120)
    private String banco;

    @Column(name = "dia_fechamento", nullable = false)
    private Short diaFechamento;

    @Column(name = "dia_vencimento", nullable = false)
    private Short diaVencimento;

    @Column(nullable = false)
    private boolean ativo = true;
}
