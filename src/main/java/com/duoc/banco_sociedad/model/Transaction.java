package com.duoc.banco_sociedad.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction_")
@Getter
@Setter
public class Transaction {

    @Id
    private Long id;

    private Long accountId;

    private BigDecimal amount;

    private String type;

    private LocalDate date;

}
