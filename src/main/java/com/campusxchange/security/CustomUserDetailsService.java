package com.campusxchange.security;

import com.campusxchange.entity.User;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by email (username in our case)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ApiException(
                            "User not found with email: " + email,
                            HttpStatus.NOT_FOUND.value(),
                            "USER_NOT_FOUND"
                    );
                });

        if (!user.getIsActive()) {
            log.warn("User account is disabled: {}", email);
            throw new ApiException(
                    "User account is disabled",
                    HttpStatus.FORBIDDEN.value(),
                    "ACCOUNT_DISABLED"
            );
        }

        return user;
    }

    /**
     * Load user by ID
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new ApiException(
                            "User not found",
                            HttpStatus.NOT_FOUND.value(),
                            "USER_NOT_FOUND"
                    );
                });

        if (!user.getIsActive()) {
            log.warn("User account is disabled: {}", user.getEmail());
            throw new ApiException(
                    "User account is disabled",
                    HttpStatus.FORBIDDEN.value(),
                    "ACCOUNT_DISABLED"
            );
        }

        return user;
    }
}
