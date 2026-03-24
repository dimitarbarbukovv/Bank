package com.example.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @NotBlank(message = "Текущата парола е задължителна")
    private String currentPassword;

    @NotBlank(message = "Новата парола е задължителна")
    @Size(min = 6, message = "Новата парола трябва да е поне 6 символа")
    private String newPassword;
}
