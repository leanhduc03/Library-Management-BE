package com.spring.LibraryManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFinesDTO {
    private Long userId;
    private String username;
    private Double totalFinesAmount;
}