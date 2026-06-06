package com.campusxchange.dto;

import com.campusxchange.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String profilePhotoUrl;
    private UserRole role;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn; // in milliseconds
    private Boolean emailVerified;
    private Boolean studentVerified;
}
