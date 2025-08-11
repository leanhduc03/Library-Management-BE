package com.spring.LibraryManagement.Service;

import com.spring.LibraryManagement.DTO.TokenPair;
import com.spring.LibraryManagement.Exception.JwtAuthenticationException;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenExpirationMs;

    public TokenPair generateTokenPair(Authentication authentication) {
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);
        return new TokenPair(accessToken, refreshToken);
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtExpirationMs, new HashMap<>());
    }

    public String generateRefreshToken(Authentication authentication) {

        Map<String, String> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        return generateToken(authentication, refreshTokenExpirationMs, claims);
    }

    public boolean validateTokenForUser(String token, UserDetails userDetails) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.warn("Token is blacklisted");
            return false;
        }
        final String username = extractUsernameFromToken(token);
        return username != null && username.equals(userDetails.getUsername());
    }

    // public boolean isValidToken(String token) {
    // if (tokenBlacklistService.isTokenBlacklisted(token)) {
    // log.warn("Token is blacklisted");
    // return false;
    // }
    // return extractUsernameFromToken(token) != null;
    // }

    public String extractUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return false;
        }
        return "refresh".equals(claims.get("tokenType"));
    }

    public Date extractExpirationDateFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        return claims.getExpiration();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT token đã hết hạn: {}", e.getMessage());
            throw new JwtAuthenticationException("Token đã hết hạn", e);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token không hợp lệ: {}", e.getMessage());
            throw new JwtAuthenticationException("Token không hợp lệ", e);
        }
    }

    private String generateToken(Authentication authentication, long expirationMs, Map<String, String> claims) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Lấy thông tin về role từ Authentication
        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .orElse("");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        JwtBuilder jwtBuilder = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate);

        // Thêm các claims từ map
        for (Map.Entry<String, String> entry : claims.entrySet()) {
            jwtBuilder.claim(entry.getKey(), entry.getValue());
        }
        jwtBuilder.claim("role", role);
        
        return jwtBuilder
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
