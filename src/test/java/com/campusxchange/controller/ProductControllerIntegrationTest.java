package com.campusxchange.controller;

import com.campusxchange.dto.CreateProductRequest;
import com.campusxchange.entity.ProductCategory;
import com.campusxchange.entity.ProductCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = CreateProductRequest.builder()
                .title("Test Laptop")
                .description("A high-quality laptop in excellent condition")
                .price(new BigDecimal("45000"))
                .originalPrice(new BigDecimal("75000"))
                .category(ProductCategory.ELECTRONICS)
                .condition(ProductCondition.GOOD)
                .college("MIT")
                .location("Campus Store")
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .build();
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProductsSuccess() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("Should create product with valid data")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateProductSuccess() throws Exception {
        mockMvc.perform(post("/api/products")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Laptop"))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"));
    }

    @Test
    @DisplayName("Should reject product with missing title")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateProductMissingTitle() throws Exception {
        createRequest.setTitle(null);

        mockMvc.perform(post("/api/products")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should reject product with invalid price")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateProductInvalidPrice() throws Exception {
        createRequest.setPrice(new BigDecimal("-1000"));

        mockMvc.perform(post("/api/products")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should get products by category")
    void testGetProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/products/category/{category}", ProductCategory.ELECTRONICS)
                .param("page", "0")
                .param("size", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get products by college")
    void testGetProductsByCollege() throws Exception {
        mockMvc.perform(get("/api/products/college/{college}", "MIT")
                .param("page", "0")
                .param("size", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should search products by keyword")
    void testSearchProducts() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("query", "laptop")
                .param("page", "0")
                .param("size", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get products with advanced filters")
    void testSearchAdvancedFilters() throws Exception {
        mockMvc.perform(get("/api/products/search/advanced")
                .param("minPrice", "10000")
                .param("maxPrice", "100000")
                .param("category", "ELECTRONICS")
                .param("college", "MIT")
                .param("page", "0")
                .param("size", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get seller's products")
    void testGetSellerProducts() throws Exception {
        mockMvc.perform(get("/api/products/seller/{sellerId}", 1)
                .param("page", "0")
                .param("size", "12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
