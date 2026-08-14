package com.duoc.banco_sociedad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "annual_statement_")
@Getter
@Setter
public class AnnualStatement {

    @Id
    private Long id;

    private Long accountId;

    private Integer year_;

    private BigDecimal totalDeposits;

    private BigDecimal totalWithdrawals;

    private BigDecimal finalBalance;

    private String status;
}
