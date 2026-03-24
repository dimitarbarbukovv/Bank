package com.example.bank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InstallmentDto {

    private Long id;
    private Integer monthNumber;
    private BigDecimal paymentAmount;
    private BigDecimal principalPart;
    private BigDecimal interestPart;
    private BigDecimal remainingPrincipal;
    private boolean paid;
}

