package com.campusxchange.dto;

import com.campusxchange.entity.ProductCategory;
import com.campusxchange.entity.ProductCondition;
import com.campusxchange.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private ProductCategory category;
    private ProductCondition condition;
    private ProductStatus status;
    private Long sellerId;
    private String sellerName;
    private Double sellerRating;
    private String college;
    private String location;
    private List<String> imageUrls;
    private String videoUrl;
    private String modelUrl;
    private Integer viewCount;
    private Double rating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
