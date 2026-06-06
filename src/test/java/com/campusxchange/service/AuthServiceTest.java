package com.campusxchange.service;

import com.campusxchange.dto.AuthRequest;
import com.campusxchange.dto.AuthResponse;
import com.campusxchange.dto.RegisterRequest;
import com.campusxchange.entity.User;
import com.campusxchange.entity.UserRole;
import com.campusxchange.exception.ApiException;
import com.campusxchange.repository.UserRepository;
import com.campusxchange.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@mit.edu")
                .firstName("Test")
                .lastName("User")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .college("MIT")
                .phoneNumber("1234567890")
                .studentId("MIT2024001")
                .acceptTerms(true)
                .build();

        authRequest = AuthRequest.builder()
                .email("test@mit.edu")
                .password("SecurePass123!")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@mit.edu")
                .firstName("Test")
                .lastName("User")
                .password("hashedPassword")
                .college("MIT")
                .phoneNumber("1234567890")
                .role(UserRole.STUDENT)
                .emailVerified(false)
                .studentVerified(false)
                .isActive(true)
                .rating(0.0)
                .totalReviews(0)
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtProvider.generateAccessToken(testUser.getEmail())).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken(testUser.getEmail())).thenReturn("refreshToken");
        when(jwtProvider.getTokenExpirationTime()).thenReturn(86400000L);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@mit.edu");
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterEmailExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("Email already registered");
    }

    @Test
    @DisplayName("Should throw exception when passwords don't match")
    void testRegisterPasswordMismatch() {
        registerRequest.setConfirmPassword("DifferentPassword");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("Passwords do not match");
    }

    @Test
    @DisplayName("Should login user successfully")
    void testLoginSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtProvider.generateAccessToken(authRequest.getEmail())).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken(authRequest.getEmail())).thenReturn("refreshToken");
        when(jwtProvider.getTokenExpirationTime()).thenReturn(86400000L);

        AuthResponse response = authService.login(authRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@mit.edu");
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        verify(userRepository, times(1)).findByEmail(authRequest.getEmail());
    }

    @Test
    @DisplayName("Should throw exception on invalid credentials")
    void testLoginInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshTokenSuccess() {
        when(jwtProvider.validateToken("refreshToken")).thenReturn(true);
        when(jwtProvider.getEmailFromToken("refreshToken")).thenReturn("test@mit.edu");
        when(userRepository.findByEmail("test@mit.edu")).thenReturn(Optional.of(testUser));
        when(jwtProvider.generateAccessToken("test@mit.edu")).thenReturn("newAccessToken");
        when(jwtProvider.generateRefreshToken("test@mit.edu")).thenReturn("newRefreshToken");
        when(jwtProvider.getTokenExpirationTime()).thenReturn(86400000L);

        AuthResponse response = authService.refreshToken("refreshToken");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("Should throw exception on invalid refresh token")
    void testRefreshTokenInvalid() {
        when(jwtProvider.validateToken("invalidToken")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("invalidToken"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("Should verify email successfully")
    void testVerifyEmailSuccess() {
        when(userRepository.findByEmail("test@mit.edu")).thenReturn(Optional.of(testUser));

        authService.verifyEmail("test@mit.edu");

        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("Should verify student successfully")
    void testVerifyStudentSuccess() {
        when(userRepository.findByEmail("test@mit.edu")).thenReturn(Optional.of(testUser));

        authService.verifyStudent("test@mit.edu");

        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getStudentVerified()).isTrue();
    }
}
