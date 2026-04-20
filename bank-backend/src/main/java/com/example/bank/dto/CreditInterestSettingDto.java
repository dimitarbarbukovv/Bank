package com.example.bank.dto;

import com.example.bank.model.CreditType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreditInterestSettingDto {

    private Long id;
    private CreditType creditType;
    private BigDecimal minIncome;
    private BigDecimal maxIncome;
    private BigDecimal interestRate;
    private BigDecimal maxDebtRatio;
    private BigDecimal minDownPaymentPct;
}