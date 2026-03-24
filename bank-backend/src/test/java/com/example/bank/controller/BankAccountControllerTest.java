package com.example.bank.controller;

import com.example.bank.dto.BankAccountDto;
import com.example.bank.dto.DepositRequestDto;
import com.example.bank.service.BankAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController bankAccountController;

    @Test
    void openDelegates() {
        BankAccountDto dto = new BankAccountDto();
        dto.setId(1L);
        when(bankAccountService.openAccount(dto)).thenReturn(dto);
        assertEquals(1L, bankAccountController.open(dto).getId());
    }

    @Test
    void byClientDelegates() {
        BankAccountDto dto = new BankAccountDto();
        when(bankAccountService.getByClient(3L)).thenReturn(List.of(dto));
        assertEquals(1, bankAccountController.byClient(3L).size());
    }

    @Test
    void closeDelegates() {
        bankAccountController.close(8L);
        verify(bankAccountService).closeAccount(8L);
    }

    @Test
    void depositDelegates() {
        DepositRequestDto in = new DepositRequestDto();
        BankAccountDto out = new BankAccountDto();
        out.setId(9L);
        when(bankAccountService.deposit(9L, in)).thenReturn(out);
        assertEquals(9L, bankAccountController.deposit(9L, in).getId());
    }
}
