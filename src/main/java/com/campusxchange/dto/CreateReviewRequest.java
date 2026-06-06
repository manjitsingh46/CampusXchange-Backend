package com.campusxchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1", message = "Rating must be at least 1")
    @DecimalMax(value = "5", message = "Rating must be at most 5")
    private BigDecimal rating;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Comment is required")
    @Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
    private String comment;

    private Boolean isVerifiedPurchase = false;
}
