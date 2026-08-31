package com.duoc.banco_sociedad.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "account_")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    private String name;

    private BigDecimal balance;

    private Integer age;

    private String type;

    private BigDecimal interestRate;

    private BigDecimal interestAmount;

    private BigDecimal finalBalance;
}