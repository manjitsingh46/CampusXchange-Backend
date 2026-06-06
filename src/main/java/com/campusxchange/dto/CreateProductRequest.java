package com.campusxchange.dto;

import com.campusxchange.entity.ProductCategory;
import com.campusxchange.entity.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    private BigDecimal originalPrice;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    @NotNull(message = "Condition is required")
    private ProductCondition condition;

    @NotBlank(message = "College is required")
    private String college;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;

    private String videoUrl;

    private String modelUrl;
}
