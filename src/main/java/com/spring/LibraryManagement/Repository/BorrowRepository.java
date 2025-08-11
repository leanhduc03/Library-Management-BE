package com.spring.LibraryManagement.Repository;

import com.spring.LibraryManagement.Entity.Borrow;
import com.spring.LibraryManagement.Entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);

    List<Borrow> findByUserId(Long userId);

    List<Borrow> findByBookId(Long bookId);

    List<Borrow> findByStatus(BorrowStatus status);

    @Query("SELECT b FROM Borrow b WHERE b.status = 'BORROWED' AND b.dueDate < :today")
    List<Borrow> findOverdueBooks(LocalDate today);

    @Query("SELECT b FROM Borrow b WHERE b.status = 'RETURNED' AND b.dueDate < b.returnDate")
    List<Borrow> findOverdueReturnedBooks();

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.book.id = :bookId AND b.status = 'BORROWED'")
    Long countActiveBorrowsByBookId(Long bookId);

    List<Borrow> findByUserIdAndStatus(Long userId, BorrowStatus status);
}