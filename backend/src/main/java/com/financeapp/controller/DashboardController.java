package com.financeapp.controller;

import com.financeapp.dto.response.ApiResponse;
import com.financeapp.dto.response.DashboardResponse;
import com.financeapp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * Roles: ADMIN, ANALYST
     * Returns aggregated financial metrics, category totals, and monthly trends.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getSummary() {
        DashboardResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
