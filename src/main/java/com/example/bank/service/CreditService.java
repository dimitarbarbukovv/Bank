package com.example.bank.service;

import com.example.bank.dto.CreditRequestDto;
import com.example.bank.dto.CreditSummaryDto;
import com.example.bank.dto.InstallmentDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.exception.NotFoundException;
import com.example.bank.mapper.CreditMapper;
import com.example.bank.model.BankAccount;
import com.example.bank.model.Credit;
import com.example.bank.model.CreditType;
import com.example.bank.model.Installment;
import com.example.bank.repository.BankAccountRepository;
import com.example.bank.repository.CreditRepository;
import com.example.bank.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final InstallmentRepository installmentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ClientService clientService;
    private final CurrentUserService currentUserService;
    private final CreditMapper creditMapper;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Long createCredit(CreditRequestDto dto) {
        validateMortgageInputs(dto);

        Credit credit = new Credit();
        credit.setClient(clientService.getById(dto.getClientId()));
        credit.setType(dto.getType());
        credit.setPrincipal(dto.getPrincipal());
        credit.setTermMonths(dto.getTermMonths());
        credit.setAnnualInterestRate(getAnnualRateForType(dto.getType(), dto.getNetIncome()));

        if (dto.getType() == CreditType.CONSUMER) {
            if (dto.getDisbursementAccountId() == null) {
                throw new BusinessException("За потребителски кредит трябва да изберете сметка за превод");
            }
            BankAccount account = bankAccountRepository.findById(dto.getDisbursementAccountId())
                    .orElseThrow(() -> new NotFoundException("Избраната сметка за превод не е намерена"));
            if (!account.getOwner().getId().equals(dto.getClientId())) {
                throw new BusinessException("Избраната сметка не принадлежи на този клиент");
            }
            credit.setDisbursementAccount(account);
        }

        BigDecimal monthlyPayment = calculateAnnuityPayment(
                credit.getPrincipal(),
                credit.getTermMonths(),
                credit.getAnnualInterestRate()
        );
        BigDecimal maxAllowedPayment = dto.getNetIncome()
                .multiply(BigDecimal.valueOf(0.30))
                .setScale(2, RoundingMode.HALF_UP);
        if (monthlyPayment.compareTo(maxAllowedPayment) > 0) {
            throw new BusinessException("Месечната вноска надвишава 30% от нетния доход");
        }

        currentUserService.getCurrentEmployee().ifPresent(credit::setCreatedBy);

        credit = creditRepository.save(credit);
        if (credit.getDisbursementAccount() != null) {
            BankAccount target = credit.getDisbursementAccount();
            target.setBalance(target.getBalance().add(credit.getPrincipal()));
            bankAccountRepository.save(target);
        }

        generateSchedule(credit);
        return credit.getId();
    }

    public BigDecimal calculateSuggestedMaxPrincipal(CreditType type, BigDecimal netIncome, Integer termMonths,
                                                     BigDecimal propertyValue, BigDecimal downPayment) {
        if (netIncome == null || termMonths == null || termMonths <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = getAnnualRateForType(type, netIncome);
        BigDecimal maxMonthly = netIncome.multiply(BigDecimal.valueOf(0.30)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlus = monthlyRate.add(BigDecimal.ONE).pow(termMonths);
        BigDecimal incomeBasedMax = maxMonthly.multiply(onePlus.subtract(BigDecimal.ONE))
                .divide(monthlyRate.multiply(onePlus), 2, RoundingMode.HALF_UP);
        if (type != CreditType.MORTGAGE || propertyValue == null || downPayment == null
                || propertyValue.compareTo(BigDecimal.ZERO) <= 0) {
            return incomeBasedMax;
        }

        BigDecimal minDownPayment = propertyValue.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
        if (downPayment.compareTo(minDownPayment) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ltvCap = propertyValue.subtract(downPayment).setScale(2, RoundingMode.HALF_UP);
        if (ltvCap.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return incomeBasedMax.min(ltvCap);
    }

    private BigDecimal getAnnualRateForType(CreditType type, BigDecimal netIncome) {
        BigDecimal income = netIncome == null ? BigDecimal.ZERO : netIncome;
        if (type == CreditType.CONSUMER) {
            if (income.compareTo(BigDecimal.valueOf(1500)) < 0) {
                return BigDecimal.valueOf(10.2);
            }
            if (income.compareTo(BigDecimal.valueOf(3000)) < 0) {
                return BigDecimal.valueOf(8.9);
            }
            return BigDecimal.valueOf(7.6);
        }
        if (income.compareTo(BigDecimal.valueOf(2500)) < 0) {
            return BigDecimal.valueOf(5.2);
        }
        return BigDecimal.valueOf(4.4);
    }

    private BigDecimal calculateAnnuityPayment(BigDecimal principal, int n, BigDecimal annualRate) {
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRPowerN = monthlyRate.add(BigDecimal.ONE).pow(n);
        return principal
                .multiply(monthlyRate.multiply(onePlusRPowerN))
                .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    private void validateMortgageInputs(CreditRequestDto dto) {
        if (dto.getType() != CreditType.MORTGAGE) {
            return;
        }
        if (dto.getPropertyValue() == null || dto.getPropertyValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("За ипотечен кредит въведете стойност на имота");
        }
        if (dto.getDownPayment() == null || dto.getDownPayment().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("За ипотечен кредит въведете валидно самоучастие");
        }
        BigDecimal minDownPayment = dto.getPropertyValue()
                .multiply(BigDecimal.valueOf(0.20))
                .setScale(2, RoundingMode.HALF_UP);
        if (dto.getDownPayment().compareTo(minDownPayment) < 0) {
            throw new BusinessException("Самоучастието трябва да е минимум 20% от стойността на имота");
        }
        BigDecimal expectedPrincipal = dto.getPropertyValue()
                .subtract(dto.getDownPayment())
                .setScale(2, RoundingMode.HALF_UP);
        if (expectedPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Размерът на кредита за ипотека трябва да е по-голям от 0");
        }
        if (dto.getPrincipal().setScale(2, RoundingMode.HALF_UP).compareTo(expectedPrincipal) != 0) {
            throw new BusinessException("Сумата на ипотечния кредит трябва да е равна на стойност на имота минус самоучастие");
        }
    }

    private void generateSchedule(Credit credit) {
        BigDecimal principal = credit.getPrincipal();
        int n = credit.getTermMonths();

        BigDecimal monthlyRate = credit.getAnnualInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal annuityPayment = calculateAnnuityPayment(principal, n, credit.getAnnualInterestRate());

        BigDecimal remaining = principal;
        for (int month = 1; month <= n; month++) {
            BigDecimal interest = remaining.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = annuityPayment.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            remaining = remaining.subtract(principalPart).setScale(2, RoundingMode.HALF_UP);
            if (month == n && remaining.compareTo(BigDecimal.ZERO) != 0) {
                principalPart = principalPart.add(remaining);
                remaining = BigDecimal.ZERO;
            }

            Installment inst = new Installment();
            inst.setCredit(credit);
            inst.setMonthNumber(month);
            inst.setPaymentAmount(annuityPayment);
            inst.setInterestPart(interest);
            inst.setPrincipalPart(principalPart);
            inst.setRemainingPrincipal(remaining.max(BigDecimal.ZERO));
            inst.setPaid(false);
            installmentRepository.save(inst);
        }
    }

    @Transactional(readOnly = true)
    public List<InstallmentDto> getSchedule(Long creditId) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new NotFoundException("Кредитът не е намерен"));
        return installmentRepository.findByCreditIdOrderByMonthNumberAsc(credit.getId())
                .stream()
                .map(creditMapper::toInstallmentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreditSummaryDto> getByClient(Long clientId) {
        return creditRepository.findByClientIdOrderByIdDesc(clientId)
                .stream()
                .map(c -> creditMapper.toSummaryDto(c, resolveStatus(c)))
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void markInstallmentPaid(Long installmentId) {
        Installment inst = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new NotFoundException("Вноската не е намерена"));
        inst.setPaid(true);
    }

    @Transactional(readOnly = true)
    public String getCreditStatus(Long creditId) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new NotFoundException("Кредитът не е намерен"));
        boolean allPaid = credit.getInstallments().stream().allMatch(Installment::isPaid);
        boolean anyPaid = credit.getInstallments().stream().anyMatch(Installment::isPaid);
        if (allPaid) {
            return "PAID";
        }
        if (anyPaid) {
            return "IN_PROGRESS";
        }
        return "NEW";
    }

    private String resolveStatus(Credit credit) {
        boolean allPaid = credit.getInstallments().stream().allMatch(Installment::isPaid);
        boolean anyPaid = credit.getInstallments().stream().anyMatch(Installment::isPaid);
        if (allPaid) {
            return "PAID";
        }
        if (anyPaid) {
            return "IN_PROGRESS";
        }
        return "NEW";
    }
}

