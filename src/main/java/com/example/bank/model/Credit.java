package com.example.bank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "credits")
@Getter
@Setter
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id")
    private BankAccount disbursementAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditType type;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @NotNull
    @Positive
    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "annual_interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

