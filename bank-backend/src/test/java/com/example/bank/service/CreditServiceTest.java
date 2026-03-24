package com.example.bank.service;

import com.example.bank.dto.CreditRequestDto;
import com.example.bank.dto.CreditSummaryDto;
import com.example.bank.dto.InstallmentDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.mapper.CreditMapper;
import com.example.bank.model.*;
import com.example.bank.repository.BankAccountRepository;
import com.example.bank.repository.CreditRepository;
import com.example.bank.repository.InstallmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private CreditRepository creditRepository;
    @Mock
    private InstallmentRepository installmentRepository;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private CurrentUserService currentUserService;
    @Spy
    private CreditMapper creditMapper = new CreditMapper();

    @InjectMocks
    private CreditService creditService;

    @Test
    void consumerCreditRequiresAccount() {
        CreditRequestDto dto = baseConsumerDto();
        dto.setDisbursementAccountId(null);
        assertThrows(BusinessException.class, () -> creditService.createCredit(dto));
    }

    @Test
    void consumerCreditRejectsForeignAccount() {
        CreditRequestDto dto = baseConsumerDto();
        dto.setDisbursementAccountId(77L);
        dto.setClientId(5L);

        IndividualClient client = new IndividualClient();
        client.setId(5L);
        client.setType(ClientType.INDIVIDUAL);
        when(clientService.getById(5L)).thenReturn(client);

        IndividualClient other = new IndividualClient();
        other.setId(6L);
        other.setType(ClientType.INDIVIDUAL);
        BankAccount acc = new BankAccount();
        acc.setOwner(other);
        when(bankAccountRepository.findById(77L)).thenReturn(Optional.of(acc));

        assertThrows(BusinessException.class, () -> creditService.createCredit(dto));
    }

    @Test
    void createConsumerCreditSuccessDisbursesAndGeneratesSchedule() {
        CreditRequestDto dto = baseConsumerDto();
        dto.setDisbursementAccountId(77L);
        dto.setClientId(5L);

        IndividualClient client = new IndividualClient();
        client.setId(5L);
        client.setType(ClientType.INDIVIDUAL);
        when(clientService.getById(5L)).thenReturn(client);

        BankAccount acc = new BankAccount();
        acc.setId(77L);
        acc.setOwner(client);
        acc.setBalance(BigDecimal.ZERO);
        when(bankAccountRepository.findById(77L)).thenReturn(Optional.of(acc));

        Employee creator = new Employee();
        creator.setUsername("admin");
        when(currentUserService.getCurrentEmployee()).thenReturn(Optional.of(creator));

        when(creditRepository.save(any(Credit.class))).thenAnswer(i -> {
            Credit c = i.getArgument(0);
            c.setId(99L);
            return c;
        });

        Long id = creditService.createCredit(dto);

        assertEquals(99L, id);
        verify(bankAccountRepository).save(any(BankAccount.class));
        verify(installmentRepository, atLeast(1)).save(any(Installment.class));
    }

    @Test
    void mortgageDownPaymentBelow20PercentThrows() {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setType(CreditType.MORTGAGE);
        dto.setClientId(1L);
        dto.setTermMonths(120);
        dto.setNetIncome(BigDecimal.valueOf(3000));
        dto.setPropertyValue(BigDecimal.valueOf(100000));
        dto.setDownPayment(BigDecimal.valueOf(10000));
        dto.setPrincipal(BigDecimal.valueOf(90000));

        assertThrows(BusinessException.class, () -> creditService.createCredit(dto));
    }

    @Test
    void calculateSuggestedMaxPrincipalForMortgageWithBadDownPaymentIsZero() {
        BigDecimal max = creditService.calculateSuggestedMaxPrincipal(
                CreditType.MORTGAGE,
                BigDecimal.valueOf(2500),
                120,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(1000)
        );
        assertEquals(BigDecimal.ZERO, max);
    }

    @Test
    void getScheduleMapsInstallments() {
        Credit c = new Credit();
        c.setId(12L);
        when(creditRepository.findById(12L)).thenReturn(Optional.of(c));

        Installment i = new Installment();
        i.setId(1L);
        i.setMonthNumber(1);
        i.setPaymentAmount(BigDecimal.TEN);
        i.setPrincipalPart(BigDecimal.ONE);
        i.setInterestPart(BigDecimal.ONE);
        i.setRemainingPrincipal(BigDecimal.valueOf(9));
        i.setPaid(false);
        when(installmentRepository.findByCreditIdOrderByMonthNumberAsc(12L)).thenReturn(List.of(i));

        List<InstallmentDto> out = creditService.getSchedule(12L);
        assertEquals(1, out.size());
        assertEquals(1, out.getFirst().getMonthNumber());
    }

    @Test
    void getByClientIncludesAuditAndStatus() {
        Employee creator = new Employee();
        creator.setUsername("admin");
        creator.setDisplayName(" ");

        Credit c = new Credit();
        c.setId(1L);
        c.setType(CreditType.CONSUMER);
        c.setPrincipal(BigDecimal.valueOf(1000));
        c.setTermMonths(12);
        c.setAnnualInterestRate(BigDecimal.valueOf(8.9));
        c.setCreatedAt(LocalDateTime.now());
        c.setCreatedBy(creator);
        Installment inst = new Installment();
        inst.setPaid(false);
        c.setInstallments(new ArrayList<>(List.of(inst)));
        when(creditRepository.findByClientIdOrderByIdDesc(5L)).thenReturn(List.of(c));

        List<CreditSummaryDto> out = creditService.getByClient(5L);
        assertEquals("NEW", out.getFirst().getStatus());
        assertEquals("admin", out.getFirst().getCreatedByDisplayName());
    }

    @Test
    void markInstallmentPaidSetsFlag() {
        Installment inst = new Installment();
        inst.setPaid(false);
        when(installmentRepository.findById(3L)).thenReturn(Optional.of(inst));

        creditService.markInstallmentPaid(3L);
        assertTrue(inst.isPaid());
    }

    @Test
    void getCreditStatusPaidWhenAllInstallmentsPaid() {
        Credit c = new Credit();
        Installment i1 = new Installment();
        i1.setPaid(true);
        Installment i2 = new Installment();
        i2.setPaid(true);
        c.setInstallments(List.of(i1, i2));
        when(creditRepository.findById(8L)).thenReturn(Optional.of(c));

        assertEquals("PAID", creditService.getCreditStatus(8L));
    }

    private static CreditRequestDto baseConsumerDto() {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setType(CreditType.CONSUMER);
        dto.setPrincipal(BigDecimal.valueOf(1000));
        dto.setTermMonths(12);
        dto.setNetIncome(BigDecimal.valueOf(4000));
        dto.setClientId(1L);
        return dto;
    }
}
