package com.example.bank.dto;

import com.example.bank.model.EmployeeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEmployeeDto {

    @NotBlank(message = "Потребителското име е задължително")
    @Size(max = 100, message = "Потребителското име е твърде дълго")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,100}$",
            message = "Потребителското име може да съдържа само букви, цифри и . _ -")
    private String username;

    @NotBlank(message = "Паролата е задължителна")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа")
    private String password;

    @NotNull(message = "Ролята е задължителна")
    private EmployeeRole role;

    private String displayName;
}
