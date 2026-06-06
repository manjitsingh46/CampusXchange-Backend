package com.campusxchange.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class GoogleAuthRequest {

    @NotBlank(message = "Google credential is required")
    private String credential;
}
