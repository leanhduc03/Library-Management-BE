package com.spring.LibraryManagement.Service;

import com.spring.LibraryManagement.DTO.*;
import com.spring.LibraryManagement.Entity.Role;
import com.spring.LibraryManagement.Entity.User;
import com.spring.LibraryManagement.Entity.VerificationToken;
import com.spring.LibraryManagement.Repository.UserRepository;
import com.spring.LibraryManagement.Repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserRepository userRepository;
    VerificationTokenRepository verificationTokenRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JwtService jwtService;
    UserDetailsService userDetailsService;
    EmailService emailService;
    TokenBlacklistService tokenBlacklistService;

    private static final String REGISTRATION_PURPOSE = "REGISTRATION";
    private static final String PASSWORD_RESET_PURPOSE = "PASSWORD_RESET";

    @Transactional
    public EmailVerificationResponse requestRegister(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        
        // Kiểm tra nếu đã có token chưa sử dụng
        Optional<VerificationToken> existingToken = verificationTokenRepository
                .findByEmailAndPurpose(request.getEmail(), REGISTRATION_PURPOSE);
        
        // Xóa token cũ nếu có
        existingToken.ifPresent(verificationTokenRepository::delete);
        
        // Tạo token mới
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        VerificationToken token = VerificationToken.generateToken(
                request.getEmail(),
                request.getUsername(),
                REGISTRATION_PURPOSE,
                encodedPassword
        );
        
        verificationTokenRepository.save(token);
        
        // Gửi email xác thực
        emailService.sendVerificationEmail(request.getEmail(), token.getToken(), false);
        
        return new EmailVerificationResponse(
                request.getUsername(),
                request.getEmail(),
                "Mã xác thực đã được gửi đến email của bạn"
        );
    }

    @Transactional
    public void confirmRegister(ConfirmRegisterRequest request) {
        // Tìm token hợp lệ
        VerificationToken token = verificationTokenRepository
                .findByTokenAndEmailAndPurposeAndUsedFalse(
                        request.getVerifyCode(),
                        request.getEmail(),
                        REGISTRATION_PURPOSE
                )
                .orElseThrow(() -> new IllegalArgumentException("Mã xác thực không hợp lệ hoặc đã hết hạn"));
        
        if (token.isExpired()) {
            verificationTokenRepository.delete(token);
            throw new IllegalArgumentException("Mã xác thực đã hết hạn");
        }
        
        if (!token.getUsername().equals(request.getUsername())) {
            throw new IllegalArgumentException("Thông tin đăng ký không khớp");
        }
        
        // Tạo tài khoản mới
        User user = User.builder()
                .username(request.getUsername())
                .password(token.getPassword()) // Sử dụng password đã mã hóa từ token
                .email(request.getEmail())
                .role(Role.ROLE_USER)
                .totalFinesAmount(0.00)
                .build();
        
        userRepository.save(user);
        
        // Đánh dấu token đã sử dụng
        token.setUsed(true);
        verificationTokenRepository.save(token);
    }

    @Transactional
    public EmailVerificationResponse requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này"));
        
        // Kiểm tra nếu đã có token chưa sử dụng
        Optional<VerificationToken> existingToken = verificationTokenRepository
                .findByEmailAndPurpose(request.getEmail(), PASSWORD_RESET_PURPOSE);
        
        // Xóa token cũ nếu có
        existingToken.ifPresent(verificationTokenRepository::delete);
        
        // Tạo token mới
        VerificationToken token = VerificationToken.generateToken(
                request.getEmail(),
                user.getUsername(),
                PASSWORD_RESET_PURPOSE,
                null
        );
        
        verificationTokenRepository.save(token);
        
        // Gửi email xác thực
        emailService.sendVerificationEmail(request.getEmail(), token.getToken(), true);
        
        return new EmailVerificationResponse(
                user.getUsername(),
                request.getEmail(),
                "Mã xác thực đã được gửi đến email của bạn"
        );
    }

    @Transactional
    public void resetPassword(ConfirmPasswordRequest request) {
        // Tìm token hợp lệ
        VerificationToken token = verificationTokenRepository
                .findByTokenAndEmailAndPurposeAndUsedFalse(
                        request.getVerifyCode(),
                        request.getEmail(),
                        PASSWORD_RESET_PURPOSE
                )
                .orElseThrow(() -> new IllegalArgumentException("Mã xác thực không hợp lệ hoặc đã hết hạn"));
        
        if (token.isExpired()) {
            verificationTokenRepository.delete(token);
            throw new IllegalArgumentException("Mã xác thực đã hết hạn");
        }
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        
        if (!user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("Email không khớp với tài khoản");
        }
        
        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Đánh dấu token đã sử dụng
        token.setUsed(true);
        verificationTokenRepository.save(token);
    }

    public TokenPair login(LoginRequest loginRequest){
        log.info("username: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        log.info("Authentication passed for username: {}", loginRequest.getUsername());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("author: {}", authentication.getAuthorities());
        return jwtService.generateTokenPair(authentication);
    }

    public void logout(String token) {
        Date expiryDate = jwtService.extractExpirationDateFromToken(token);
        tokenBlacklistService.blacklistToken(token, expiryDate);
    }

    public TokenPair refreshToken(@Valid RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if(!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtService.extractUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if(userDetails == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        String accessToken = jwtService.generateAccessToken(authentication);
        return new TokenPair(accessToken, refreshToken);
    }
}
