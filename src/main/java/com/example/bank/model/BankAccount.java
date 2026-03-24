package com.example.bank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iban", nullable = false, unique = true, length = 34)
    private String iban;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private AccountCurrency currency = AccountCurrency.EUR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;
}

