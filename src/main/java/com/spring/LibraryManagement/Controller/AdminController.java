package com.spring.LibraryManagement.Controller;


import com.spring.LibraryManagement.DTO.UserDTO;
import com.spring.LibraryManagement.Entity.User;
import com.spring.LibraryManagement.Mapper.UserMapper;
import com.spring.LibraryManagement.Service.AdminService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    AdminService adminService;
    UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDTOS = adminService.findAllUsers()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return adminService.findUserById(id)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @PathVariable Long id) {
        User updatedUser = adminService.updateUser(userDTO, id);
        if (updatedUser != null) {
            return ResponseEntity.ok(userMapper.toDto(updatedUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
