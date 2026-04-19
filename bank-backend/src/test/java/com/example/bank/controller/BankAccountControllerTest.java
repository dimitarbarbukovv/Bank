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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void openCallsServiceOnce() {
        BankAccountDto dto = new BankAccountDto();

        when(bankAccountService.openAccount(dto)).thenReturn(dto);

        bankAccountController.open(dto);

        verify(bankAccountService, times(1)).openAccount(dto);
    }

    @Test
    void openWithNullDoesNotCrash() {
        when(bankAccountService.openAccount(null))
                .thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class,
                () -> bankAccountController.open(null));
    }

    @Test
    void byClientReturnsEmptyList() {
        when(bankAccountService.getByClient(99L)).thenReturn(List.of());

        assertTrue(bankAccountController.byClient(99L).isEmpty());
    }

    @Test
    void depositForwardsCorrectArguments() {
        DepositRequestDto dto = new DepositRequestDto();

        when(bankAccountService.deposit(10L, dto))
                .thenReturn(new BankAccountDto());

        bankAccountController.deposit(10L, dto);

        verify(bankAccountService).deposit(10L, dto);
    }

    @Test
    void closeCanBeCalledMultipleTimes() {
        bankAccountController.close(1L);
        bankAccountController.close(1L);

        verify(bankAccountService, times(2)).closeAccount(1L);
    }

    @Test
    void controllerDoesNothingWithoutCall() {
        verifyNoInteractions(bankAccountService);
    }




}
