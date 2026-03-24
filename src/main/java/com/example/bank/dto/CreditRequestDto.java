package com.example.bank.dto;

import com.example.bank.model.CreditType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditRequestDto {

    @NotNull(message = "Трябва да изберете клиент")
    private Long clientId;

    @NotNull(message = "Видът кредит е задължителен")
    private CreditType type;

    @NotNull(message = "Сумата е задължителна")
    @DecimalMin(value = "100.00", message = "Минималната сума за кредит е 100 лв.")
    private BigDecimal principal;

    @NotNull(message = "Срокът е задължителен")
    @Positive(message = "Срокът трябва да е положително число")
    private Integer termMonths;

    @NotNull(message = "Нетният доход е задължителен")
    @DecimalMin(value = "0.01", message = "Нетният доход трябва да е по-голям от 0")
    private BigDecimal netIncome;

    private Long disbursementAccountId;

    private BigDecimal propertyValue;

    private BigDecimal downPayment;
}

