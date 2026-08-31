package com.duoc.banco_sociedad.repository;

import com.duoc.banco_sociedad.model.AnnualTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnnualTransactionRepository
        extends JpaRepository<AnnualTransaction, Long> {

    @Query("""
            SELECT DISTINCT t.accountId
            FROM AnnualTransaction t
            ORDER BY t.accountId
            """)
    List<Long> findDistinctAccountIds();

    List<AnnualTransaction> findByAccountId(Long accountId);
}
