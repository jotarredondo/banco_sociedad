package com.duoc.banco_sociedad.repository;

import com.duoc.banco_sociedad.model.DailyTransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTransactionSummaryRepository
        extends JpaRepository<DailyTransactionSummary, Long> {
}
