package com.spring.LibraryManagement.Mapper;

import com.spring.LibraryManagement.DTO.FineDTO;
import com.spring.LibraryManagement.Entity.Fine;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FineMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "borrow.id", target = "borrowId")
    @Mapping(source = "borrow.book.title", target = "bookTitle")
    @Mapping(source = "fineAmount", target = "fineAmount")
    @Mapping(source = "lastUpdated", target = "lastUpdated")
    @Mapping(source = "isPaid", target = "isPaid")
    FineDTO toFineDTO(Fine fine);
}