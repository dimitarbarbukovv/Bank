package com.example.bank.dto;

import com.example.bank.model.CreditType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditSummaryDto {
    private Long id;
    private CreditType type;
    private BigDecimal principal;
    private Integer termMonths;
    private BigDecimal annualInterestRate;
    private LocalDateTime createdAt;
    private String status;
    private String createdByUsername;
    private String createdByDisplayName;
}

