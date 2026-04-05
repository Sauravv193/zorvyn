package com.financeapp.service;

import com.financeapp.dto.response.DashboardResponse;
import com.financeapp.dto.response.DashboardResponse.CategoryTotal;
import com.financeapp.dto.response.DashboardResponse.MonthlyTrend;
import com.financeapp.entity.FinancialRecord.RecordType;
import com.financeapp.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int TREND_MONTHS = 6;
    private static final DateTimeFormatter MONTH_KEY   = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    private final FinancialRecordRepository repository;

    /**
     * Computes the full dashboard summary:
     * - Total income / expenses / net balance
     * - Category-wise aggregates
     * - Monthly trends for the last 6 months
     */
    public DashboardResponse getSummary() {
        BigDecimal totalIncome   = repository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = repository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);
        Long       totalRecords  = repository.countAll();

        List<CategoryTotal> categoryTotals = buildCategoryTotals();
        List<MonthlyTrend>  monthlyTrends  = buildMonthlyTrends();

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .categoryTotals(categoryTotals)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    // ── Category Totals ───────────────────────────────────────────────────

    /**
     * Aggregates records grouped by (category, type), sorted by total amount desc.
     * Raw DB result columns: [category(String), type(RecordType), sum(BigDecimal), count(Long)]
     */
    private List<CategoryTotal> buildCategoryTotals() {
        List<Object[]> rows = repository.findCategoryTotals();

        return rows.stream()
                .map(row -> CategoryTotal.builder()
                        .category((String) row[0])
                        .type(((RecordType) row[1]).name())
                        .totalAmount((BigDecimal) row[2])
                        .count((Long) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    // ── Monthly Trends ────────────────────────────────────────────────────

    /**
     * Produces a list of MonthlyTrend covering the last TREND_MONTHS calendar months,
     * including months with zero activity so the chart has no gaps.
     *
     * Raw DB result columns: [month(String "yyyy-MM"), type(RecordType), sum(BigDecimal)]
     */
    private List<MonthlyTrend> buildMonthlyTrends() {
        LocalDate since = LocalDate.now()
                .withDayOfMonth(1)
                .minusMonths(TREND_MONTHS - 1L);

        List<Object[]> rows = repository.findMonthlyTrends(since);

        // Seed an ordered map with every month in the window, all zeroed out
        Map<String, MonthlyTrend> trendMap = buildEmptyTrendMap(since);

        // Populate actual values from DB rows
        for (Object[] row : rows) {
            String     monthKey = (String) row[0];
            RecordType type     = (RecordType) row[1];
            BigDecimal amount   = (BigDecimal) row[2];

            MonthlyTrend trend = trendMap.get(monthKey);
            if (trend == null) continue; // outside our window

            if (type == RecordType.INCOME) {
                trend.setIncome(amount);
            } else {
                trend.setExpenses(amount);
            }
        }

        // Compute net for each month and return as ordered list
        trendMap.values().forEach(t ->
                t.setNet(t.getIncome().subtract(t.getExpenses())));

        return new ArrayList<>(trendMap.values());
    }

    /**
     * Builds a LinkedHashMap (preserving insertion/month order) covering
     * the last TREND_MONTHS months, each initialized to zero amounts.
     */
    private Map<String, MonthlyTrend> buildEmptyTrendMap(LocalDate since) {
        Map<String, MonthlyTrend> map = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(since);
        YearMonth now    = YearMonth.now();

        while (!cursor.isAfter(now)) {
            String monthKey   = cursor.format(MONTH_KEY);
            String monthLabel = cursor.atDay(1).format(MONTH_LABEL);

            map.put(monthKey, MonthlyTrend.builder()
                    .month(monthKey)
                    .monthLabel(monthLabel)
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());

            cursor = cursor.plusMonths(1);
        }
        return map;
    }
}
