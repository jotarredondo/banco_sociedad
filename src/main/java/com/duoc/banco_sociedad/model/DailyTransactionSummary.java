package com.duoc.banco_sociedad.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_transaction_summary")
@Getter
@Setter
public class DailyTransactionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime executionDate;

    private long totalRead;

    private long totalWritten;

    private long totalSkipped;

    private long totalCredits;

    private long totalDebits;

    private BigDecimal totalCreditAmount;

    private BigDecimal totalDebitAmount;

    private String status;

    private long executionTimeMs;
}
