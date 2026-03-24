package com.example.bank.dto;

import com.example.bank.model.EmployeeRole;
import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;
    private String username;
    private String displayName;
    private EmployeeRole role;
    private boolean active;
}
