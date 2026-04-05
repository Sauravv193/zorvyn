package com.financeapp.controller;

import com.financeapp.dto.request.FinancialRecordRequest;
import com.financeapp.dto.response.ApiResponse;
import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.entity.FinancialRecord.RecordType;
import com.financeapp.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    /**
     * GET /api/records
     * Roles: ADMIN, ANALYST, VIEWER
     * Supports filtering by type, category, date range.
     * Supports pagination and sorting.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getAll(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        Page<FinancialRecordResponse> records =
                recordService.findAll(type, category, dateFrom, dateTo, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<FinancialRecordResponse>>builder()
                        .success(true)
                        .data(records)
                        .page(records.getNumber())
                        .size(records.getSize())
                        .totalElements(records.getTotalElements())
                        .totalPages(records.getTotalPages())
                        .build());
    }

    /**
     * GET /api/records/{id}
     * Roles: ADMIN, ANALYST, VIEWER
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.findById(id)));
    }

    /**
     * POST /api/records
     * Roles: ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> create(
            @Valid @RequestBody FinancialRecordRequest request) {

        FinancialRecordResponse created = recordService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Record created successfully.", created));
    }

    /**
     * PUT /api/records/{id}
     * Roles: ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {

        FinancialRecordResponse updated = recordService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Record updated successfully.", updated));
    }

    /**
     * DELETE /api/records/{id}
     * Roles: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Record deleted successfully.", null));
    }
}
