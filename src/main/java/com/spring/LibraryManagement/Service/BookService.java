package com.spring.LibraryManagement.Service;


import com.spring.LibraryManagement.DTO.BookDTO;
import com.spring.LibraryManagement.Entity.Book;
import com.spring.LibraryManagement.Repository.BookRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookService {
    BookRepository bookRepository;

    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> findBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookDTO bookDTO) {
        return findBookById(id)
                .map(existingBook -> {
                    existingBook.setTitle(bookDTO.getTitle());
                    existingBook.setAuthor(bookDTO.getAuthor());
                    existingBook.setIsbn(bookDTO.getIsbn());
                    existingBook.setImageUrl(bookDTO.getImageUrl());
                    existingBook.setCategory(bookDTO.getCategory());
                    existingBook.setAvailableCopies(bookDTO.getAvailableCopies());
                    existingBook.setTotalCopies(bookDTO.getTotalCopies());
                    return saveBook(existingBook);
                })
                .orElse(null);
    }

    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

}
