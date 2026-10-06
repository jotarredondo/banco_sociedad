package com.duoc.banco_sociedad.repository;

import com.duoc.banco_sociedad.model.AnnualStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnnualStatementRepository
        extends JpaRepository<AnnualStatement, Long> {

    @Query(value = """
            SELECT
                cuenta_id,
                YEAR(fecha) AS year_,
                SUM(CASE
                    WHEN transaccion = 'deposito'
                    THEN monto
                    ELSE 0
                END) AS total_depositos,
                SUM(CASE
                    WHEN transaccion IN ('retiro', 'compra', 'pago')
                    THEN monto
                    ELSE 0
                END) AS total_egresos,
                COUNT(*) AS cantidad_movimientos
            FROM annual_statement_
            GROUP BY cuenta_id, YEAR(fecha)
            ORDER BY cuenta_id, YEAR(fecha)
            """,
            nativeQuery = true)
    List<Object[]> obtenerResumenAnual();
}
