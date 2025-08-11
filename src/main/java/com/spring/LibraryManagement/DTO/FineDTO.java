package com.spring.LibraryManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineDTO {
    private Long id;
    private Long borrowId;
    private String bookTitle;
    private Double fineAmount;
    private LocalDateTime lastUpdated;
    private Boolean isPaid;
}