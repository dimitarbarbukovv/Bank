package com.example.bank.service;

import com.example.bank.dto.BankAccountDto;
import com.example.bank.dto.DepositRequestDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.mapper.BankAccountMapper;
import com.example.bank.model.*;
import com.example.bank.repository.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private CurrentUserService currentUserService;
    @Spy
    private BankAccountMapper bankAccountMapper = new BankAccountMapper();

    @InjectMocks
    private BankAccountService bankAccountService;

    @Test
    void openAccountCreatesIbanAndAudit() {
        IndividualClient owner = new IndividualClient();
        owner.setId(3L);
        owner.setType(ClientType.INDIVIDUAL);
        Employee creator = new Employee();
        creator.setUsername("admin");
        creator.setDisplayName("Admin");

        when(clientService.getById(3L)).thenReturn(owner);
        when(currentUserService.getCurrentEmployee()).thenReturn(Optional.of(creator));
        when(bankAccountRepository.findByIban(any())).thenReturn(Optional.empty());
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> {
            BankAccount a = i.getArgument(0);
            a.setId(11L);
            return a;
        });

        BankAccountDto dto = new BankAccountDto();
        dto.setClientId(3L);
        dto.setBalance(BigDecimal.valueOf(100));

        BankAccountDto out = bankAccountService.openAccount(dto);
        assertEquals(11L, out.getId());
        assertNotNull(out.getIban());
        assertEquals("Admin", out.getCreatedByDisplayName());
    }

    @Test
    void getByClientMapsCurrencyDefault() {
        IndividualClient owner = new IndividualClient();
        owner.setId(1L);
        owner.setType(ClientType.INDIVIDUAL);
        when(clientService.getById(1L)).thenReturn(owner);

        BankAccount a = new BankAccount();
        a.setId(1L);
        a.setIban("BG00BANK12345678901234");
        a.setOwner(owner);
        a.setStatus(AccountStatus.ACTIVE);
        a.setBalance(BigDecimal.TEN);
        a.setCurrency(null);
        when(bankAccountRepository.findByOwner_IdOrderByIdDesc(1L)).thenReturn(List.of(a));

        List<BankAccountDto> out = bankAccountService.getByClient(1L);
        assertEquals(AccountCurrency.EUR, out.getFirst().getCurrency());
    }

    @Test
    void openAccountWithBgnThrows() {
        IndividualClient owner = new IndividualClient();
        owner.setId(3L);
        owner.setType(ClientType.INDIVIDUAL);
        when(clientService.getById(3L)).thenReturn(owner);

        BankAccountDto dto = new BankAccountDto();
        dto.setClientId(3L);
        dto.setBalance(BigDecimal.ZERO);
        dto.setCurrency(AccountCurrency.BGN);

        assertThrows(BusinessException.class, () -> bankAccountService.openAccount(dto));
    }

    @Test
    void closeAccountWithNonZeroBalanceThrows() {
        BankAccount a = new BankAccount();
        a.setBalance(BigDecimal.ONE);
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(a));
        assertThrows(BusinessException.class, () -> bankAccountService.closeAccount(2L));
    }

    @Test
    void depositToClosedAccountThrows() {
        BankAccount a = new BankAccount();
        a.setStatus(AccountStatus.CLOSED);
        when(bankAccountRepository.findById(9L)).thenReturn(Optional.of(a));
        DepositRequestDto dto = new DepositRequestDto();
        dto.setAmount(BigDecimal.ONE);

        assertThrows(BusinessException.class, () -> bankAccountService.deposit(9L, dto));
    }

    @Test
    void depositAddsBalance() {
        IndividualClient owner = new IndividualClient();
        owner.setId(7L);
        owner.setType(ClientType.INDIVIDUAL);
        BankAccount a = new BankAccount();
        a.setId(9L);
        a.setIban("BG00BANK123");
        a.setOwner(owner);
        a.setStatus(AccountStatus.ACTIVE);
        a.setCurrency(AccountCurrency.BGN);
        a.setBalance(BigDecimal.TEN);
        when(bankAccountRepository.findById(9L)).thenReturn(Optional.of(a));

        DepositRequestDto dto = new DepositRequestDto();
        dto.setAmount(BigDecimal.valueOf(5));
        BankAccountDto out = bankAccountService.deposit(9L, dto);

        assertEquals(BigDecimal.valueOf(15), out.getBalance());
    }
}
