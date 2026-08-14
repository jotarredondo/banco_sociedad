package com.duoc.banco_sociedad.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "account_")
@Getter
@Setter
public class Account {

    @Id
    private Long id;

    private String type;

    private BigDecimal balance;

    private BigDecimal interestRate;
}
