package com.spring.LibraryManagement.DTO;

import com.spring.LibraryManagement.Entity.Borrow;
import com.spring.LibraryManagement.Entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Role role;
}
