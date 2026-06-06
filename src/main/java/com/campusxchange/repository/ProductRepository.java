package com.campusxchange.repository;

import com.campusxchange.entity.Product;
import com.campusxchange.entity.ProductCategory;
import com.campusxchange.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.status = 'AVAILABLE' AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Product> findAllAvailableProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = 'AVAILABLE' AND p.deletedAt IS NULL")
    Page<Product> findBySellerIdAndAvailable(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.status = 'AVAILABLE' AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Product> findByCategory(@Param("category") ProductCategory category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.college = :college AND p.status = 'AVAILABLE' AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Product> findByCollege(@Param("college") String college, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.deletedAt IS NULL")
    Page<Product> findByStatus(@Param("status") ProductStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = 'AVAILABLE'
        AND p.deletedAt IS NULL
        AND p.price BETWEEN :minPrice AND :maxPrice
        AND p.category = :category
        AND p.college = :college
        ORDER BY p.createdAt DESC
    """)
    Page<Product> searchProducts(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("category") ProductCategory category,
        @Param("college") String college,
        Pageable pageable
    );

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = 'AVAILABLE'
        AND p.deletedAt IS NULL
        AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY p.createdAt DESC
    """)
    Page<Product> searchByTitleOrDescription(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'AVAILABLE' AND p.deletedAt IS NULL AND p.createdAt > :since ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(@Param("since") LocalDateTime since);

    @Query("SELECT p FROM Product p WHERE p.status = 'AVAILABLE' AND p.deletedAt IS NULL ORDER BY p.rating DESC LIMIT :limit")
    List<Product> findTopRatedProducts(@Param("limit") int limit);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.status = 'AVAILABLE'")
    long countActiveBySeller(@Param("sellerId") Long sellerId);
}
