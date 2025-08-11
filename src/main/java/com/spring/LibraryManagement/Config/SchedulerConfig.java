package com.spring.LibraryManagement.Config;

import com.spring.LibraryManagement.Service.BorrowService;
import com.spring.LibraryManagement.Service.FineService;
import com.spring.LibraryManagement.Service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
    
    private final BorrowService borrowService;
    private final TokenBlacklistService tokenBlacklistService;
    private final FineService fineService;  // Thêm FineService

    @Scheduled(fixedRate = 3600000)
    public void updateOverdueStatus() {
//        log.info("Bắt đầu chạy updateOverdueStatus lúc: {}", new Date());
        borrowService.updateOverdueStatus();
//        log.info("Kết thúc updateOverdueStatus");
    }

    @Scheduled(fixedRate = 60000)
    public void updateFines() {
//        log.info("Bắt đầu chạy updateFines lúc: {}", new Date());
        fineService.updateFines();
//        log.info("Kết thúc updateFines");
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredTokens() {
//        log.info("Bắt đầu chạy cleanupExpiredTokens lúc: {}", new Date());
        tokenBlacklistService.cleanupExpiredTokens();
//        log.info("Kết thúc cleanupExpiredTokens");
    }
}