package com.spring.LibraryManagement.Controller;

import com.spring.LibraryManagement.DTO.BookDTO;
import com.spring.LibraryManagement.Entity.Book;
import com.spring.LibraryManagement.Mapper.BookMapper;
import com.spring.LibraryManagement.Service.BookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {
    BookService bookService;
    BookMapper bookMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_BOOK_READ')")
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> bookDTOs = bookService.findAllBooks().stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_BOOK_READ')")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return bookService.findBookById(id)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_BOOK_CREATE')")
    public ResponseEntity<BookDTO> saveBook(@RequestBody BookDTO bookDTO) {
        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(bookMapper.toDto(savedBook));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_BOOK_UPDATE')")
    public ResponseEntity<BookDTO> updateBook(@RequestBody BookDTO bookDTO, @PathVariable Long id) {
        Book updatedBook = bookService.updateBook(id, bookDTO);
        if (updatedBook != null) {
            return ResponseEntity.ok(bookMapper.toDto(updatedBook));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_BOOK_DELETE')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (bookService.findBookById(id).isPresent()) {
            bookService.deleteBookById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
