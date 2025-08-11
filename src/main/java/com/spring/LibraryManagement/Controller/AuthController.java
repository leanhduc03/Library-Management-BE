package com.spring.LibraryManagement.Controller;

import com.spring.LibraryManagement.DTO.*;
import com.spring.LibraryManagement.Service.AuthService;
import com.spring.LibraryManagement.Service.JwtService;
import com.spring.LibraryManagement.Service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    // @PostMapping("/register")
    // public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
    // authService.register(request);
    // return ResponseEntity.ok("User registered successfully");
    // }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenPair tokenPair = authService.login(loginRequest);
        log.info("Received login request for email: {}", loginRequest.getUsername());
        return ResponseEntity.ok(tokenPair);

    }

    @PostMapping("/request-register")
    public ResponseEntity<EmailVerificationResponse> requestRegister(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.requestRegister(request));
    }

    @PostMapping("/confirm-register")
    public ResponseEntity<Void> confirmRegister(@RequestBody @Valid ConfirmRegisterRequest request) {
        authService.confirmRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<EmailVerificationResponse> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ConfirmPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = authService.refreshToken(request);
        return ResponseEntity.ok(tokenPair);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ"));
        }

        try {
            String token = authHeader.substring(7);
            authService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi đăng xuất", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể đăng xuất. Vui lòng thử lại."));
        }
    }

}
