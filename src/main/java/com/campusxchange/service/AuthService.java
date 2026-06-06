package com.campusxchange.service;

import com.campusxchange.dto.AuthRequest;
import com.campusxchange.dto.AuthResponse;
import com.campusxchange.dto.RegisterRequest;
import com.campusxchange.entity.User;
import com.campusxchange.entity.UserRole;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.UserRepository;
import com.campusxchange.security.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("Passwords do not match", HttpStatus.BAD_REQUEST.value(), "PASSWORD_MISMATCH");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT.value(), "EMAIL_EXISTS");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username already taken", HttpStatus.CONFLICT.value(), "USERNAME_EXISTS");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .college(request.getCollege())
                .studentId(request.getStudentId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STUDENT)
                .emailVerified(true)   // auto-verified — no email infra required
                .studentVerified(false)
                .twoFactorEnabled(false)
                .isActive(true)
                .rating(0.0)
                .totalReviews(0)
                .build();

        user = userRepository.save(user);
        log.info("User registered with id: {}", user.getId());

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        return buildResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtProvider.generateAccessToken(request.getEmail());
            String refreshToken = jwtProvider.generateRefreshToken(request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return buildResponse(user, accessToken, refreshToken);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Authentication failed for email: {}", request.getEmail(), ex);
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED.value(), "INVALID_CREDENTIALS");
        }
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public AuthResponse googleLogin(String credential) {
        log.info("Google OAuth login attempt");

        // Verify token with Google's public endpoint
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;
        Map<String, String> tokenInfo;
        try {
            RestTemplate restTemplate = new RestTemplate();
            tokenInfo = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new ApiException("Invalid Google token", HttpStatus.UNAUTHORIZED.value(), "INVALID_GOOGLE_TOKEN");
        }

        if (tokenInfo == null) {
            throw new ApiException("Google token verification returned null", HttpStatus.UNAUTHORIZED.value(), "INVALID_GOOGLE_TOKEN");
        }

        // Validate audience matches our client ID (skip check if env not set)
        String aud = tokenInfo.get("aud");
        if (!googleClientId.isEmpty() && !googleClientId.equals(aud)) {
            throw new ApiException("Google token audience mismatch", HttpStatus.UNAUTHORIZED.value(), "INVALID_GOOGLE_TOKEN");
        }

        if (!"true".equals(tokenInfo.get("email_verified"))) {
            throw new ApiException("Google account email is not verified", HttpStatus.UNAUTHORIZED.value(), "EMAIL_NOT_VERIFIED");
        }

        String email = tokenInfo.get("email");
        String firstName = tokenInfo.getOrDefault("given_name", "");
        String lastName = tokenInfo.getOrDefault("family_name", "");
        String picture = tokenInfo.get("picture");

        // Find existing user or create one
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
            if (baseUsername.length() < 3) baseUsername = "user" + baseUsername;
            String username = baseUsername;
            int suffix = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + suffix++;
            }

            return User.builder()
                    .email(email)
                    .username(username)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .profilePhotoUrl(picture)
                    .role(UserRole.STUDENT)
                    .emailVerified(true)
                    .studentVerified(false)
                    .isActive(true)
                    .rating(0.0)
                    .totalReviews(0)
                    .build();
        });

        // Sync Google profile photo for existing users if they don't have one
        if (user.getProfilePhotoUrl() == null && picture != null) {
            user.setProfilePhotoUrl(picture);
        }
        user.setLastLogin(LocalDateTime.now());
        user = userRepository.save(user);

        log.info("Google login successful for user id: {}", user.getId());

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        return buildResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ApiException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED.value(), "INVALID_REFRESH_TOKEN");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND"));

        String newAccessToken = jwtProvider.generateAccessToken(email);
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        return buildResponse(user, newAccessToken, newRefreshToken);
    }

    public void inititateForgotPassword(String email) {
        // Stub — no email infrastructure yet. Log only; never reveal if user exists.
        log.info("Forgot-password requested for email: {}", email);
        userRepository.findByEmail(email).ifPresent(user ->
            log.info("Would send reset link to userId={}", user.getId())
        );
    }

    @Transactional
    public void verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void verifyStudent(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND"));
        user.setStudentVerified(true);
        userRepository.save(user);
    }

    private AuthResponse buildResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProvider.getTokenExpirationTime())
                .emailVerified(user.getEmailVerified())
                .studentVerified(user.getStudentVerified())
                .build();
    }
}
