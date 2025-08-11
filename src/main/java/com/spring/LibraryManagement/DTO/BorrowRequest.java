package com.spring.LibraryManagement.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowRequest {

    @NotNull(message = "ID của sách không được để trống")
    Long bookId;

    @NotNull(message = "Ngày trả dự kiến không được để trống")
    LocalDate dueDate;
}