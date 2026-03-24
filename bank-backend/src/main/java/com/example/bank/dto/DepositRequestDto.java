package com.example.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequestDto {

    @NotNull(message = "Сумата за внасяне е задължителна")
    @DecimalMin(value = "0.01", message = "Сумата за внасяне трябва да е по-голяма от 0")
    private BigDecimal amount;
}
