package com.financeapp.dto.response;

import com.financeapp.entity.User.Role;
import com.financeapp.entity.User.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private Role role;
        private UserStatus status;
    }
}
