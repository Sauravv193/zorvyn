package com.financeapp.controller;

import com.financeapp.dto.response.ApiResponse;
import com.financeapp.dto.response.UserResponse;
import com.financeapp.entity.User;
import com.financeapp.entity.User.UserStatus;
import com.financeapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAll()));
    }

    /**
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    /**
     * PATCH /api/users/{id}/status?status=INACTIVE
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {

        return ResponseEntity.ok(
                ApiResponse.ok("User status updated.", userService.setStatus(id, status)));
    }

    /**
     * PATCH /api/users/{id}/role?role=ANALYST
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long id,
            @RequestParam User.Role role) {

        return ResponseEntity.ok(
                ApiResponse.ok("User role updated.", userService.setRole(id, role)));
    }

    /**
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted.", null));
    }
}
