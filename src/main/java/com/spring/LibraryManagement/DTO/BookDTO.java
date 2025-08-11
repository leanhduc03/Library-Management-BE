package com.spring.LibraryManagement.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String imageUrl;
    private LocalDate publishDate;
    private Long availableCopies;
    private Long totalCopies;
}
