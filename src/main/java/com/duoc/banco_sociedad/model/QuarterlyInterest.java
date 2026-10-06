package com.duoc.banco_sociedad.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "quarterly_interest")
@Getter
@Setter
public class QuarterlyInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaId;

    private String nombre;

    private BigDecimal saldo;

    private Integer edad;

    private String tipo;

    private BigDecimal interesCalculado;

    private BigDecimal saldoFinal;
}