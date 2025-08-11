package com.spring.LibraryManagement.Service;

import com.spring.LibraryManagement.DTO.BorrowDTO;
import com.spring.LibraryManagement.DTO.BorrowRequest;
import com.spring.LibraryManagement.Entity.*;
import com.spring.LibraryManagement.Exception.ResourceNotFoundException;
import com.spring.LibraryManagement.Mapper.BorrowMapper;
import com.spring.LibraryManagement.Repository.BookRepository;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowService {

    BorrowRepository borrowRepository;
    UserRepository userRepository;
    BookRepository bookRepository;
    BorrowMapper borrowMapper;
    FineRepository fineRepository;  // Thêm repository cho Fine

    @Transactional
    public BorrowDTO borrowBook(BorrowRequest request) {
        // Lấy người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + request.getBookId()));

        // Kiểm tra số lượng sách còn lại
        Long borrowedCopies = borrowRepository.countActiveBorrowsByBookId(book.getId());
        if (book.getAvailableCopies() <= borrowedCopies) {
            throw new RuntimeException("Sách đã hết, không thể mượn");
        }

        // Tạo bản ghi mượn sách
        Borrow borrow = Borrow.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .status(BorrowStatus.BORROWED)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Borrow savedBorrow = borrowRepository.save(borrow);

        return borrowMapper.toBorrowDTO(savedBorrow);
    }

    private double calculateFineAmount(LocalDate dueDate, LocalDate returnDate) {
        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);

        if (daysLate <= 0) {
            return 0;
        }

        // Số đợt 2 ngày (làm tròn lên)
        long periods = (daysLate + 1) / 2;

        // Tiền phạt: 20.000đ cho 2 ngày đầu, sau đó cấp số cộng
        double fineAmount = 0;
        for (int i = 1; i <= periods; i++) {
            fineAmount += 20000;
        }

        return fineAmount;
    }

    @Transactional
    public BorrowDTO returnBook(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi mượn sách với ID: " + borrowId));

        Book book = bookRepository.findBooksByBorrowId(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với borrow ID: " + borrowId));

        // Kiểm tra quyền - chỉ người mượn hoặc ADMIN mới được trả sách
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (!borrow.getUser().getUsername().equals(username) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Bạn không có quyền trả sách này");
        }

        if (borrow.getStatus() != BorrowStatus.BORROWED && borrow.getStatus() != BorrowStatus.OVERDUE) {
            throw new RuntimeException("Sách đã được trả hoặc đang trong trạng thái không hợp lệ");
        }

        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(BorrowStatus.RETURNED);

        // Xử lý tiền phạt nếu trả sách trễ
        if (LocalDate.now().isAfter(borrow.getDueDate())) {
            double fineAmount = calculateFineAmount(borrow.getDueDate(), LocalDate.now());

            // Tìm hoặc tạo mới bản ghi fine
            Fine fine = fineRepository.findByBorrowId(borrowId)
                    .orElse(Fine.builder()
                            .borrow(borrow)
                            .fineAmount(fineAmount)
                            .isPaid(false)
                            .lastUpdated(LocalDateTime.now())
                            .build());

            if (fine.getId() != null) {
                // Cập nhật fine hiện có
                fine.setFineAmount(fineAmount);
                fine.setLastUpdated(LocalDateTime.now());
            }

            fineRepository.save(fine);

            // Cập nhật tổng tiền phạt của người dùng
            User user = borrow.getUser();
            Double totalFines = fineRepository.sumUnpaidFinesByUserId(user.getId());
            user.setTotalFinesAmount(totalFines != null ? totalFines : 0.0);
            userRepository.save(user);
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Borrow updatedBorrow = borrowRepository.save(borrow);

        return borrowMapper.toBorrowDTO(updatedBorrow);
    }

    @Transactional(readOnly = true)
    public List<BorrowDTO> getAllBorrows() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin có thể xem tất cả các bản ghi mượn sách
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return borrowRepository.findAll().stream()
                    .map(borrowMapper::toBorrowDTO)
                    .collect(Collectors.toList());
        } else {
            // Người dùng chỉ có thể xem các bản ghi mượn sách của chính họ
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            return borrowRepository.findByUserId(user.getId()).stream()
                    .map(borrowMapper::toBorrowDTO)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public BorrowDTO getBorrowById(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy bản ghi mượn sách với ID: " + borrowId));

        // Kiểm tra quyền - chỉ người mượn hoặc ADMIN mới được xem chi tiết
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (!borrow.getUser().getUsername().equals(username) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin mượn sách này");
        }

        return borrowMapper.toBorrowDTO(borrow);
    }

    @Transactional(readOnly = true)
    public List<BorrowDTO> getOverdueBooks() {
        LocalDate today = LocalDate.now();
        List<Borrow> overdueBooks = borrowRepository.findOverdueBooks(today);

        return overdueBooks.stream()
                .map(borrowMapper::toBorrowDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateOverdueStatus() {
        LocalDate today = LocalDate.now();
        List<Borrow> activeBooks = borrowRepository.findByStatus(BorrowStatus.BORROWED);

        activeBooks.stream()
                .filter(borrow -> borrow.getDueDate().isBefore(today))
                .forEach(borrow -> {
                    borrow.setStatus(BorrowStatus.OVERDUE);
                    borrowRepository.save(borrow);
                });
    }

}