package com.financeapp.repository;

import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.FinancialRecord.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>,
                JpaSpecificationExecutor<FinancialRecord> {

    // --- Aggregation for Dashboard ---

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("SELECT COUNT(r) FROM FinancialRecord r")
    Long countAll();

    // Category totals — grouped by category and type
    @Query("""
            SELECT r.category, r.type, SUM(r.amount), COUNT(r)
            FROM FinancialRecord r
            GROUP BY r.category, r.type
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> findCategoryTotals();

    // Monthly trends for last N months
    @Query("""
            SELECT TO_CHAR(r.recordDate, 'YYYY-MM') AS month,
                   r.type,
                   SUM(r.amount)
            FROM FinancialRecord r
            WHERE r.recordDate >= :since
            GROUP BY TO_CHAR(r.recordDate, 'YYYY-MM'), r.type
            ORDER BY month
            """)
    List<Object[]> findMonthlyTrends(@Param("since") LocalDate since);

    // Existence check for delete safety
    boolean existsByCreatedByIdAndId(Long userId, Long recordId);
}
