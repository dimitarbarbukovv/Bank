package com.example.bank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "installments")
@Getter
@Setter
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", nullable = false)
    private Credit credit;

    @NotNull
    @Positive
    @Column(name = "month_number", nullable = false)
    private Integer monthNumber;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "principal_part", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalPart;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "interest_part", nullable = false, precision = 19, scale = 2)
    private BigDecimal interestPart;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "remaining_principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingPrincipal;

    @Column(name = "paid", nullable = false)
    private boolean paid = false;
}

