package com.campusxchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    private Long id;
    private Long productId;
    private String productTitle;
    private Long reviewerId;
    private String reviewerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal rating;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Integer unhelpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
