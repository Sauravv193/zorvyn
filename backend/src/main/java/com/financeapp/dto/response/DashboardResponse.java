package com.financeapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Long totalRecords;

    private List<CategoryTotal> categoryTotals;
    private List<MonthlyTrend> monthlyTrends;

    @Data
    @Builder
    public static class CategoryTotal {
        private String category;
        private BigDecimal totalAmount;
        private String type;
        private Long count;
    }

    @Data
    @Builder
    public static class MonthlyTrend {
        private String month;       // "2024-01"
        private String monthLabel;  // "Jan 2024"
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}
