package com.spring.LibraryManagement.Controller;

import com.spring.LibraryManagement.DTO.BorrowDTO;
import com.spring.LibraryManagement.DTO.BorrowRequest;
import com.spring.LibraryManagement.Service.BorrowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowController {

    BorrowService borrowService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_BORROW_CREATE')")
    public ResponseEntity<BorrowDTO> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowService.borrowBook(request));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAuthority('PERMISSION_BORROW_UPDATE')")
    public ResponseEntity<BorrowDTO> returnBook(@PathVariable("id") Long borrowId) {
        return ResponseEntity.ok(borrowService.returnBook(borrowId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_BORROW_READ')")
    public ResponseEntity<List<BorrowDTO>> getAllBorrows() {
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_BORROW_READ')")
    public ResponseEntity<BorrowDTO> getBorrowById(@PathVariable("id") Long borrowId) {
        return ResponseEntity.ok(borrowService.getBorrowById(borrowId));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BorrowDTO>> getOverdueBooks() {
        return ResponseEntity.ok(borrowService.getOverdueBooks());
    }
}