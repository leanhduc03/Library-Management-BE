package com.spring.LibraryManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPasswordRequest {
    private String username;
    private String email;
    private String newPassword;
    private String verifyCode;
}
