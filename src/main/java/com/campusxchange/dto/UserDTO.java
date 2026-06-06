package com.campusxchange.dto;

import com.campusxchange.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String college;
    private String studentId;
    private String profilePhotoUrl;
    private String bio;
    private UserRole role;
    private Double rating;
    private Integer totalReviews;
    private Boolean emailVerified;
    private Boolean studentVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isVerified() {
        return emailVerified != null && emailVerified &&
               studentVerified != null && studentVerified;
    }
}
