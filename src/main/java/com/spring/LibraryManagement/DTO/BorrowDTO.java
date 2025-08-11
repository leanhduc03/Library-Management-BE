package com.spring.LibraryManagement.DTO;

import com.spring.LibraryManagement.Entity.BorrowStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowDTO {
    Long id;
    Long userId;
    String username;
    Long bookId;
    String bookTitle;
    LocalDateTime borrowDate;
    LocalDate dueDate;
    LocalDateTime returnDate;
    BorrowStatus status;
}