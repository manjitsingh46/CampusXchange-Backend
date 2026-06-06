package com.campusxchange.service;

import com.campusxchange.dto.CreateProductRequest;
import com.campusxchange.dto.ProductDTO;
import com.campusxchange.entity.Product;
import com.campusxchange.entity.ProductCategory;
import com.campusxchange.entity.ProductStatus;
import com.campusxchange.entity.User;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.ProductRepository;
import com.campusxchange.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJsonString(List<String> list) {
        try {
            return list == null ? "[]" : objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> toStringList(String json) {
        try {
            return json == null || json.isBlank() ? Collections.emptyList()
                    : objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    @Transactional
    public ProductDTO createProduct(Long sellerId, CreateProductRequest request) {
        log.info("Creating product for seller: {}", sellerId);

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(
                        "Seller not found",
                        HttpStatus.NOT_FOUND.value(),
                        "SELLER_NOT_FOUND"
                ));

        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .category(request.getCategory())
                .condition(request.getCondition())
                .seller(seller)
                .college(request.getCollege())
                .location(request.getLocation())
                .imageUrls(toJsonString(request.getImageUrls()))
                .videoUrl(request.getVideoUrl())
                .modelUrl(request.getModelUrl())
                .status(ProductStatus.AVAILABLE)
                .viewCount(0)
                .rating(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        product = productRepository.save(product);
        log.info("Product created with id: {}", product.getId());

        return mapToDTO(product);
    }
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(
                        "Product not found",
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND"
                ));

        // Increment view count
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return mapToDTO(product);
    }
    @Transactional
    public ProductDTO updateProduct(Long productId, Long sellerId, CreateProductRequest request) {
        log.info("Updating product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(
                        "Product not found",
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND"
                ));

        // Verify seller owns the product
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ApiException(
                    "You don't have permission to update this product",
                    HttpStatus.FORBIDDEN.value(),
                    "PERMISSION_DENIED"
            );
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setLocation(request.getLocation());
        product.setImageUrls(toJsonString(request.getImageUrls()));
        product.setVideoUrl(request.getVideoUrl());
        product.setModelUrl(request.getModelUrl());

        product = productRepository.save(product);
        return mapToDTO(product);
    }
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        log.info("Deleting product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(
                        "Product not found",
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND"
                ));

        // Verify seller owns the product
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ApiException(
                    "You don't have permission to delete this product",
                    HttpStatus.FORBIDDEN.value(),
                    "PERMISSION_DENIED"
            );
        }

        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
    }
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAllAvailableProducts(pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(ProductCategory category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCollege(String college, Pageable pageable) {
        return productRepository.findByCollege(college, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String query, Pageable pageable) {
        return productRepository.searchByTitleOrDescription(query, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProductsAdvanced(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductCategory category,
            String college,
            Pageable pageable) {
        return productRepository.searchProducts(minPrice, maxPrice, category, college, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<ProductDTO> getSellerProducts(Long sellerId, Pageable pageable) {
        userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(
                        "Seller not found",
                        HttpStatus.NOT_FOUND.value(),
                        "SELLER_NOT_FOUND"
                ));

        return productRepository.findBySellerIdAndAvailable(sellerId, pageable)
                .map(this::mapToDTO);
    }
    @Transactional
    public void markAsSold(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(
                        "Product not found",
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND"
                ));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ApiException(
                    "You don't have permission to modify this product",
                    HttpStatus.FORBIDDEN.value(),
                    "PERMISSION_DENIED"
            );
        }

        product.setStatus(ProductStatus.SOLD);
        productRepository.save(product);
    }
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .category(product.getCategory())
                .condition(product.getCondition())
                .status(product.getStatus())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getFullName())
                .sellerRating(product.getSeller().getRating())
                .college(product.getCollege())
                .location(product.getLocation())
                .imageUrls(toStringList(product.getImageUrls()))
                .videoUrl(product.getVideoUrl())
                .modelUrl(product.getModelUrl())
                .viewCount(product.getViewCount())
                .rating(product.getRating() != null ? product.getRating().doubleValue() : null)
                .totalReviews(product.getTotalReviews())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
