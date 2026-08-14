package com.duoc.banco_sociedad.repository;

import com.duoc.banco_sociedad.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {
}
