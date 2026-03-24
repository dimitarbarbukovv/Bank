package com.example.bank.dto;

import com.example.bank.model.EmployeeRole;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmployeeDto {
    private String displayName;
    private EmployeeRole role;
    private Boolean active;
    /** Ако е подадена, паролата се сменя (само от админ) */
    @Size(min = 6, message = "Новата парола трябва да е поне 6 символа")
    private String newPassword;
}
