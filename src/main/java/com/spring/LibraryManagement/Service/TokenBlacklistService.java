package com.spring.LibraryManagement.Service;

import com.spring.LibraryManagement.Entity.BlacklistedToken;
import com.spring.LibraryManagement.Repository.BlacklistedTokenRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@AllArgsConstructor
public class TokenBlacklistService {
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklistToken(String token, Date expiryDate) {
        log.info("Blacklisting token with expiry date: {}", expiryDate);
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiryDate(expiryDate);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        log.info("Checking if token is blacklisted: {}", token);
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired blacklisted tokens");
        blacklistedTokenRepository.deleteByExpiryDateBefore(new Date());
    }
}