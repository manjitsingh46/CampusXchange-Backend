package com.campusxchange.service;

import com.campusxchange.dto.CreateReviewRequest;
import com.campusxchange.dto.ReviewDTO;
import com.campusxchange.entity.Product;
import com.campusxchange.entity.Review;
import com.campusxchange.entity.User;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.ProductRepository;
import com.campusxchange.repository.ReviewRepository;
import com.campusxchange.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
@Slf4j
@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;
    @Transactional
    public ReviewDTO createReview(Long reviewerId, CreateReviewRequest request) {
        log.info("Creating review for product: {} by user: {}", request.getProductId(), reviewerId);

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ApiException(
                        "Reviewer not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApiException(
                        "Product not found",
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND"
                ));

        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new ApiException(
                        "Seller not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        // Check if review already exists
        reviewRepository.findByProductIdAndReviewerId(request.getProductId(), reviewerId)
                .ifPresent(r -> {
                    throw new ApiException(
                            "You have already reviewed this product",
                            HttpStatus.CONFLICT.value(),
                            "REVIEW_EXISTS"
                    );
                });

        Review review = Review.builder()
                .product(product)
                .reviewer(reviewer)
                .seller(seller)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(request.getIsVerifiedPurchase())
                .helpfulCount(0)
                .unhelpfulCount(0)
                .build();

        review = reviewRepository.save(review);

        // Update product rating
        updateProductRating(product.getId());

        // Update seller rating
        updateSellerRating(seller.getId());

        log.info("Review created with id: {}", review.getId());
        return mapToDTO(review);
    }
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getProductReviews(Long productId, Pageable pageable) {
        log.info("Getting reviews for product: {}", productId);

        productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(
                        "Product not found",
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND"
                ));

        return reviewRepository.findByProductId(productId, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getSellerReviews(Long sellerId, Pageable pageable) {
        log.info("Getting reviews for seller: {}", sellerId);

        userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(
                        "Seller not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return reviewRepository.findBysellerId(sellerId, pageable)
                .map(this::mapToDTO);
    }
    @Transactional
    public ReviewDTO updateReview(Long reviewId, Long reviewerId, CreateReviewRequest request) {
        log.info("Updating review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(
                        "Review not found",
                        HttpStatus.NOT_FOUND.value(),
                        "REVIEW_NOT_FOUND"
                ));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new ApiException(
                    "You don't have permission to update this review",
                    HttpStatus.FORBIDDEN.value(),
                    "PERMISSION_DENIED"
            );
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        // Update seller rating
        updateSellerRating(review.getSeller().getId());

        return mapToDTO(review);
    }
    @Transactional
    public void deleteReview(Long reviewId, Long reviewerId) {
        log.info("Deleting review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(
                        "Review not found",
                        HttpStatus.NOT_FOUND.value(),
                        "REVIEW_NOT_FOUND"
                ));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new ApiException(
                    "You don't have permission to delete this review",
                    HttpStatus.FORBIDDEN.value(),
                    "PERMISSION_DENIED"
            );
        }

        reviewRepository.deleteById(reviewId);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        // Update seller rating
        updateSellerRating(review.getSeller().getId());
    }
    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            Double avgRating = reviewRepository.getProductAverageRating(productId);
            long reviewCount = reviewRepository.countReviewsForProduct(productId);

            product.setRating(BigDecimal.valueOf(avgRating != null ? avgRating : 0.0));
            product.setTotalReviews((int) reviewCount);
            productRepository.save(product);
        }
    }
    private void updateSellerRating(Long sellerId) {
        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller != null) {
            Double avgRating = reviewRepository.getSellerAverageRating(sellerId);
            long reviewCount = reviewRepository.countReviewsForSeller(sellerId);

            seller.setRating(avgRating != null ? avgRating : 0.0);
            seller.setTotalReviews((int) reviewCount);
            userRepository.save(seller);
        }
    }
    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productTitle(review.getProduct().getTitle())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getFullName())
                .sellerId(review.getSeller().getId())
                .sellerName(review.getSeller().getFullName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .unhelpfulCount(review.getUnhelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
