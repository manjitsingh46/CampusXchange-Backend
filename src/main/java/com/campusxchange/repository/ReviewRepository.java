package com.campusxchange.repository;

import com.campusxchange.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.seller.id = :sellerId ORDER BY r.createdAt DESC")
    Page<Review> findBysellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId ORDER BY r.createdAt DESC")
    Page<Review> findByReviewerId(@Param("reviewerId") Long reviewerId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :sellerId")
    Double getSellerAverageRating(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.seller.id = :sellerId")
    long countReviewsForSeller(@Param("sellerId") Long sellerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getProductAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countReviewsForProduct(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.isVerifiedPurchase = true AND r.product.id = :productId ORDER BY r.helpfulCount DESC")
    List<Review> findVerifiedReviewsForProduct(@Param("productId") Long productId);

    Optional<Review> findByProductIdAndReviewerId(Long productId, Long reviewerId);
}
