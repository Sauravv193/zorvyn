package com.financeapp.service;

import com.financeapp.dto.request.FinancialRecordRequest;
import com.financeapp.dto.response.ApiResponse;
import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.FinancialRecord.RecordType;
import com.financeapp.entity.User;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.mapper.FinancialRecordMapper;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.FinancialRecordSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialRecordService {

    private final FinancialRecordRepository repository;
    private final FinancialRecordMapper     mapper;

    // ── Read ──────────────────────────────────────────────────────────────

    public Page<FinancialRecordResponse> findAll(
            RecordType type,
            String category,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {

        Specification<FinancialRecord> spec =
                FinancialRecordSpecification.withFilters(type, category, dateFrom, dateTo);

        return repository.findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    public FinancialRecordResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    // ── Write (ADMIN only — enforced via @PreAuthorize in controller) ─────

    @Transactional
    public FinancialRecordResponse create(FinancialRecordRequest request) {
        User currentUser = currentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .recordDate(request.getRecordDate())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        return mapper.toResponse(repository.save(record));
    }

    @Transactional
    public FinancialRecordResponse update(Long id, FinancialRecordRequest request) {
        FinancialRecord existing = getOrThrow(id);

        existing.setAmount(request.getAmount());
        existing.setType(request.getType());
        existing.setCategory(request.getCategory().trim());
        existing.setRecordDate(request.getRecordDate());
        existing.setDescription(request.getDescription());

        return mapper.toResponse(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("FinancialRecord", id);
        }
        repository.deleteById(id);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private FinancialRecord getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
