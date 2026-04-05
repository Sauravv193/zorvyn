package com.financeapp.repository;

import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.FinancialRecord.RecordType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class FinancialRecordSpecification {

    private FinancialRecordSpecification() {}

    /**
     * Builds a composite Specification from the provided optional filters.
     * All non-null parameters are ANDed together.
     */
    public static Specification<FinancialRecord> withFilters(
            RecordType type,
            String category,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("category")),
                        "%" + category.toLowerCase() + "%"
                ));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
