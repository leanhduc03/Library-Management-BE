package com.spring.LibraryManagement.Mapper;

import com.spring.LibraryManagement.DTO.BorrowDTO;
import com.spring.LibraryManagement.Entity.Borrow;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BorrowMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    BorrowDTO toBorrowDTO(Borrow borrow);
}
