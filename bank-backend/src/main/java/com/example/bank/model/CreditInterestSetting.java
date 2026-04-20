package com.example.bank.model;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
public class CreditInterestSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_type")
    private CreditType creditType;

    @Column(name = "min_income")
    private BigDecimal minIncome;

    @Column(name = "max_income")
    private BigDecimal maxIncome; // nullable

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "max_debt_ratio", nullable = false)
    private BigDecimal maxDebtRatio;     

    @Column(name = "min_down_payment_pct")
    private BigDecimal minDownPaymentPct;

        public BigDecimal getMaxDebtRatio() {
        return maxDebtRatio;
    }

    public void setMaxDebtRatio(BigDecimal maxDebtRatio) {
        this.maxDebtRatio = maxDebtRatio;
    }

    public BigDecimal getMinDownPaymentPct() {
        return minDownPaymentPct;
    }

    public void setMinDownPaymentPct(BigDecimal minDownPaymentPct) {
        this.minDownPaymentPct = minDownPaymentPct;
    }
}