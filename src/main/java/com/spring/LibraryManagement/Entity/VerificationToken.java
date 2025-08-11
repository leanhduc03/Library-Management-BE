package com.spring.LibraryManagement.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    String token;
    
    @Column(nullable = false)
    String email;
    
    String username;
    
    @Column(nullable = false)
    String purpose; // "REGISTRATION" hoặc "PASSWORD_RESET"
    
    @Column(nullable = false)
    LocalDateTime expiryDate;
    
    String password; // Mật khẩu (đã mã hóa) dùng cho đăng ký
    
    boolean used;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    public static VerificationToken generateToken(String email, String username, String purpose, String password) {
        return VerificationToken.builder()
                .token(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .email(email)
                .username(username)
                .purpose(purpose)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // Hết hạn sau 15 phút
                .password(password)
                .used(false)
                .build();
    }
}