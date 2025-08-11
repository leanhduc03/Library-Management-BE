package com.spring.LibraryManagement.Mapper;

import com.spring.LibraryManagement.DTO.UserDTO;
import com.spring.LibraryManagement.Entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO userDTO);
    List<UserDTO> toDto(List<User> users);
}
