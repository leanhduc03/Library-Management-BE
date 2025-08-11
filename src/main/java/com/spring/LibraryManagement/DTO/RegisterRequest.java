package com.spring.LibraryManagement.DTO;

import com.spring.LibraryManagement.Entity.Borrow;
import com.spring.LibraryManagement.Entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private Role role;
    // private Set<Borrow> borrows;
}
