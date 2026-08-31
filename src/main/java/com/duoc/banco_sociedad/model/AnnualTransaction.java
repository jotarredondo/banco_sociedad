package com.duoc.banco_sociedad.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "annual_transaction")
@Getter
@Setter
public class AnnualTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    private LocalDate date;

    private String transactionType;

    private BigDecimal amount;

    private String description;
}
