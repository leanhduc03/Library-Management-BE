package com.spring.LibraryManagement.Controller;

import com.spring.LibraryManagement.DTO.FineDTO;
import com.spring.LibraryManagement.DTO.UserFinesDTO;
import com.spring.LibraryManagement.Service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    // API cho người dùng xem tiền phạt của họ
    @GetMapping("/my-fines")
    public ResponseEntity<List<FineDTO>> getCurrentUserFines() {
        return ResponseEntity.ok(fineService.getCurrentUserFines());
    }

    // API cho người dùng xem tổng tiền phạt của họ
    @GetMapping("/my-total")
    public ResponseEntity<Double> getCurrentUserTotalFines() {
        return ResponseEntity.ok(fineService.getCurrentUserTotalFines());
    }

    // API cho admin xem tổng tiền phạt của tất cả người dùng
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserFinesDTO>> getAllUsersFines() {
        return ResponseEntity.ok(fineService.getAllUsersFines());
    }

    // API cho admin xem chi tiết tiền phạt của một người dùng
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FineDTO>> getUserFinesById(@PathVariable Long userId) {
        return ResponseEntity.ok(fineService.getUserFinesById(userId));
    }

    // API để đánh dấu tiền phạt đã được thanh toán
    @PutMapping("/{fineId}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markFineAsPaid(@PathVariable Long fineId) {
        fineService.markFineAsPaid(fineId);
        return ResponseEntity.ok().build();
    }
}