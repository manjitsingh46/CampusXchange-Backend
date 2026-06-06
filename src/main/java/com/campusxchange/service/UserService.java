package com.campusxchange.service;

import com.campusxchange.dto.UserDTO;
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

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userId) {
        log.info("Getting user profile for id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return mapToDTO(user);
    }
    @Transactional
    public UserDTO updateUserProfile(Long userId, UserDTO updateRequest) {
        log.info("Updating user profile for id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(updateRequest.getProfilePhotoUrl());
        }
        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
        }

        user = userRepository.save(user);
        log.info("User profile updated: {}", userId);

        return mapToDTO(user);
    }
    @Transactional(readOnly = true)
    public Page<?> getUserProducts(Long userId, Pageable pageable) {
        log.info("Getting products for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return productRepository.findBySellerIdAndAvailable(userId, pageable);
    }
    @Transactional(readOnly = true)
    public Page<?> getUserReviews(Long userId, Pageable pageable) {
        log.info("Getting reviews for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        return reviewRepository.findBysellerId(userId, pageable);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStats(Long userId) {
        log.info("Getting stats for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND"
                ));

        long activeListings = productRepository.countActiveBySeller(userId);
        long totalReviews = reviewRepository.countReviewsForSeller(userId);
        Double averageRating = reviewRepository.getSellerAverageRating(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("username", user.getUsername());
        stats.put("fullName", user.getFullName());
        stats.put("college", user.getCollege());
        stats.put("role", user.getRole());
        stats.put("activeListings", activeListings);
        stats.put("totalReviews", totalReviews);
        stats.put("averageRating", averageRating != null ? averageRating : 0.0);
        stats.put("emailVerified", user.getEmailVerified());
        stats.put("studentVerified", user.getStudentVerified());
        stats.put("joinedDate", user.getCreatedAt());

        return stats;
    }
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String name, Pageable pageable) {
        log.info("Searching users with name: {}", name);

        return userRepository.searchByFullName(name, pageable)
                .map(this::mapToDTO);
    }
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByCollege(String college, Pageable pageable) {
        log.info("Getting users from college: {}", college);

        return userRepository.findByCollege(college, pageable)
                .map(this::mapToDTO);
    }
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .college(user.getCollege())
                .studentId(user.getStudentId())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .rating(user.getRating())
                .totalReviews(user.getTotalReviews())
                .emailVerified(user.getEmailVerified())
                .studentVerified(user.getStudentVerified())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
