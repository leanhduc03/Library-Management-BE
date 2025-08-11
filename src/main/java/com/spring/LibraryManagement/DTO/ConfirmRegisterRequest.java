package com.spring.LibraryManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String verifyCode;
}