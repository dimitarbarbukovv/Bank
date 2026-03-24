package com.example.bank.dto;

import com.example.bank.model.AccountStatus;
import com.example.bank.model.AccountCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccountDto {

    private Long id;

    // IBAN is generated on server side
    private String iban;

    @NotNull(message = "Началната наличност е задължителна")
    @DecimalMin(value = "0.00", message = "Началната наличност не може да е отрицателна")
    private BigDecimal balance;

    private AccountStatus status;

    @NotNull(message = "Валутата е задължителна")
    private AccountCurrency currency;

    @NotNull(message = "Трябва да изберете клиент")
    private Long clientId;

    private String createdByUsername;
    private String createdByDisplayName;
}

