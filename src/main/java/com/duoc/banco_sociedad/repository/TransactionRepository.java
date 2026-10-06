package com.duoc.banco_sociedad.repository;

import com.duoc.banco_sociedad.model.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
        extends JpaRepository<DailyTransaction, Long> {
}
