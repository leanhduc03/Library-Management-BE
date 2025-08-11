package com.spring.LibraryManagement.Repository;

import com.spring.LibraryManagement.Entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByBorrowUserId(Long userId);
    
    Optional<Fine> findByBorrowId(Long borrowId);
    
    @Query("SELECT SUM(f.fineAmount) FROM Fine f WHERE f.borrow.user.id = :userId AND f.isPaid = false")
    Double sumUnpaidFinesByUserId(Long userId);
}