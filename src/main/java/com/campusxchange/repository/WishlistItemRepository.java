package com.campusxchange.repository;

import com.campusxchange.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT w FROM WishlistItem w WHERE w.user.id = :userId ORDER BY w.addedAt DESC")
    Page<WishlistItem> findByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT COUNT(w) FROM WishlistItem w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(w) FROM WishlistItem w WHERE w.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
