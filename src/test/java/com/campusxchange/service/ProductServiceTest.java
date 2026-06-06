package com.campusxchange.service;

import com.campusxchange.dto.CreateProductRequest;
import com.campusxchange.dto.ProductDTO;
import com.campusxchange.entity.Product;
import com.campusxchange.entity.ProductCategory;
import com.campusxchange.entity.ProductCondition;
import com.campusxchange.entity.ProductStatus;
import com.campusxchange.entity.User;
import com.campusxchange.entity.UserRole;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.ProductRepository;
import com.campusxchange.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private CreateProductRequest createRequest;
    private Product testProduct;
    private User testSeller;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testSeller = User.builder()
                .id(1L)
                .username("seller")
                .email("seller@mit.edu")
                .firstName("John")
                .lastName("Seller")
                .college("MIT")
                .role(UserRole.STUDENT)
                .isActive(true)
                .rating(4.5)
                .totalReviews(10)
                .build();

        createRequest = CreateProductRequest.builder()
                .title("MacBook Pro 2023")
                .description("Like new MacBook Pro with 16GB RAM and 512GB SSD")
                .price(new BigDecimal("50000"))
                .originalPrice(new BigDecimal("80000"))
                .category(ProductCategory.ELECTRONICS)
                .condition(ProductCondition.LIKE_NEW)
                .college("MIT")
                .location("Building A")
                .imageUrls(Arrays.asList("url1", "url2"))
                .build();

        testProduct = Product.builder()
                .id(1L)
                .title("MacBook Pro 2023")
                .description("Like new MacBook Pro with 16GB RAM and 512GB SSD")
                .price(new BigDecimal("50000"))
                .originalPrice(new BigDecimal("80000"))
                .category(ProductCategory.ELECTRONICS)
                .condition(ProductCondition.LIKE_NEW)
                .status(ProductStatus.AVAILABLE)
                .seller(testSeller)
                .college("MIT")
                .location("Building A")
                .imageUrls("[\"url1\",\"url2\"]")
                .viewCount(0)
                .rating(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        pageable = PageRequest.of(0, 12);
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProductSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO result = productService.createProduct(1L, createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("MacBook Pro 2023");
        assertThat(result.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(result.getSellerId()).isEqualTo(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when seller not found")
    void testCreateProductSellerNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(999L, createRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("Seller not found");
    }

    @Test
    @DisplayName("Should get product by ID")
    void testGetProductByIdSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("MacBook Pro 2023");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProductSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        createRequest.setTitle("Updated MacBook Pro");
        ProductDTO result = productService.updateProduct(1L, 1L, createRequest);

        assertThat(result).isNotNull();
        assertThat(testProduct.getTitle()).isEqualTo("Updated MacBook Pro");
    }

    @Test
    @DisplayName("Should throw exception when updating product not owned by user")
    void testUpdateProductPermissionDenied() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> productService.updateProduct(1L, 999L, createRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("You don't have permission to update this product");
    }

    @Test
    @DisplayName("Should delete product (soft delete)")
    void testDeleteProductSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.deleteProduct(1L, 1L);

        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.DELETED);
        assertThat(testProduct.getDeletedAt()).isNotNull();
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Should get all available products")
    void testGetAllProductsSuccess() {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageable, 1);
        when(productRepository.findAllAvailableProducts(pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("MacBook Pro 2023");
    }

    @Test
    @DisplayName("Should get products by category")
    void testGetProductsByCategorySuccess() {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageable, 1);
        when(productRepository.findByCategory(ProductCategory.ELECTRONICS, pageable))
                .thenReturn(productPage);

        Page<ProductDTO> result = productService.getProductsByCategory(ProductCategory.ELECTRONICS, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get seller's products")
    void testGetSellerProductsSuccess() {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageable, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
        when(productRepository.findBySellerIdAndAvailable(1L, pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getSellerProducts(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should mark product as sold")
    void testMarkAsSoldSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.markAsSold(1L, 1L);

        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.SOLD);
        verify(productRepository, times(1)).save(testProduct);
    }
}
