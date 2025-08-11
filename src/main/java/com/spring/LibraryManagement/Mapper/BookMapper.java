package com.spring.LibraryManagement.Mapper;

import com.spring.LibraryManagement.DTO.BookDTO;
import com.spring.LibraryManagement.Entity.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDTO toDto(Book book);
    Book toEntity(BookDTO bookDTO);
}
