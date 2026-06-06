package com.campusxchange.repository;

import com.campusxchange.entity.User;
import com.campusxchange.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.emailVerified = true")
    Page<User> findAllActiveVerifiedUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findByRoleAndActive(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.college = :college AND u.isActive = true ORDER BY u.rating DESC")
    Page<User> findBySellersInCollege(@Param("college") String college, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) AND u.isActive = true")
    Page<User> searchByFullName(@Param("name") String name, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.studentVerified = true AND u.emailVerified = true ORDER BY u.rating DESC")
    List<User> findTopVerifiedSellers();

    Page<User> findByCollege(String college, Pageable pageable);
}
