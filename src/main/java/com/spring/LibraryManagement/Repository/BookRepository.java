package com.spring.LibraryManagement.Repository;

import com.spring.LibraryManagement.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.Optional;


public interface BookRepository extends JpaRepository<Book, Long > {
    @Query("SELECT b FROM Book b JOIN Borrow br ON b.id = br.book.id WHERE br.id = :borrowId")
    Optional<Book> findBooksByBorrowId(Long borrowId);
}
