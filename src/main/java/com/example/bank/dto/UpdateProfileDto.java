package com.example.bank.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDto {
    @Size(max = 200, message = "Името за показване може да е до 200 символа")
    private String displayName;
}
