package com.financeapp.dto.response;

import com.financeapp.entity.FinancialRecord.RecordType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FinancialRecordResponse {
    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;
    private LocalDate recordDate;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
