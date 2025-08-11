package com.spring.LibraryManagement.Service;

import com.spring.LibraryManagement.DTO.FineDTO;
import com.spring.LibraryManagement.DTO.UserFinesDTO;
import com.spring.LibraryManagement.Entity.Borrow;
import com.spring.LibraryManagement.Entity.BorrowStatus;
import com.spring.LibraryManagement.Entity.Fine;
import com.spring.LibraryManagement.Entity.User;
import com.spring.LibraryManagement.Exception.ResourceNotFoundException;
import com.spring.LibraryManagement.Mapper.FineMapper;
import com.spring.LibraryManagement.Repository.BorrowRepository;
import com.spring.LibraryManagement.Repository.FineRepository;
import com.spring.LibraryManagement.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FineService {

    FineRepository fineRepository;
    BorrowRepository borrowRepository;
    UserRepository userRepository;
    FineMapper fineMapper;

    @Transactional
    public void updateFines() {
        LocalDate today = LocalDate.now();
        
        // Lấy tất cả các borrow đang trong trạng thái mượn và quá hạn
        List<Borrow> overdueBorrows = borrowRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, today);
        
        for (Borrow borrow : overdueBorrows) {
            // Tính toán tiền phạt dựa vào số ngày quá hạn
            long daysOverdue = ChronoUnit.DAYS.between(borrow.getDueDate(), today);
            double fineAmount = calculateFineAmount(daysOverdue);
            
            // Tìm fine hiện có hoặc tạo mới
            Fine fine = fineRepository.findByBorrowId(borrow.getId())
                    .orElse(Fine.builder()
                            .borrow(borrow)
                            .fineAmount(0.0)
                            .isPaid(false)
                            .build());
            
            // Cập nhật số tiền phạt và thời gian cập nhật
            fine.setFineAmount(fineAmount);
            fine.setLastUpdated(LocalDateTime.now());
            fineRepository.save(fine);
            
            // Cập nhật tổng tiền phạt của user
            updateUserTotalFines(borrow.getUser().getId());
        }
    }

    @Transactional(readOnly = true)
    public List<FineDTO> getCurrentUserFines() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        
        return fineRepository.findByBorrowUserId(user.getId()).stream()
                .map(fineMapper::toFineDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Double getCurrentUserTotalFines() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        
        return user.getTotalFinesAmount();
    }
    
    @Transactional(readOnly = true)
    public List<UserFinesDTO> getAllUsersFines() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin này");
        }
        
        return userRepository.findAll().stream()
                .map(user -> UserFinesDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .totalFinesAmount(user.getTotalFinesAmount())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<FineDTO> getUserFinesById(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin này");
        }
        
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        
        return fineRepository.findByBorrowUserId(userId).stream()
                .map(fineMapper::toFineDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void markFineAsPaid(Long fineId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra quyền - chỉ ADMIN mới được thanh toán tiền phạt
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật khoản tiền phạt này");
        }
        
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản tiền phạt với ID: " + fineId));
        
        fine.setIsPaid(true);
        fine.setLastUpdated(LocalDateTime.now());
        fineRepository.save(fine);
        
        // Cập nhật tổng tiền phạt của user
        updateUserTotalFines(fine.getBorrow().getUser().getId());
    }

    /**
     * Tính toán tiền phạt dựa vào số ngày quá hạn
     */
    private double calculateFineAmount(long daysOverdue) {
        if (daysOverdue <= 0) {
            return 0;
        }
        
        // Số đợt 2 ngày (làm tròn lên)
        long periods = (daysOverdue + 1) / 2;
        
        // Tiền phạt: 20.000đ cho 2 ngày đầu, sau đó cấp số cộng
        double fineAmount = 0;
        for (int i = 1; i <= periods; i++) {
            fineAmount += 20000;
        }
        
        return fineAmount;
    }
    
    /**
     * Cập nhật tổng tiền phạt của người dùng
     */
    @Transactional
    public void updateUserTotalFines(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        
        Double totalUnpaidFines = fineRepository.sumUnpaidFinesByUserId(userId);
        user.setTotalFinesAmount(totalUnpaidFines != null ? totalUnpaidFines : 0.0);
        userRepository.save(user);
    }
}