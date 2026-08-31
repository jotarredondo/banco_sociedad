package com.duoc.banco_sociedad.repository;

import com.duoc.banco_sociedad.model.AnnualStatement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnualStatementRepository
        extends JpaRepository<AnnualStatement, Long> {
}
