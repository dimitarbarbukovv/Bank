package com.example.bank.controller;

import com.example.bank.dto.BankAccountDto;
import com.example.bank.dto.DepositRequestDto;
import com.example.bank.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:5173")
public class BankAccountController {

    private BankAccountService bankAccountService;

    @Autowired
    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    public BankAccountDto open(@Valid @RequestBody BankAccountDto dto) {
        return bankAccountService.openAccount(dto);
    }

    @GetMapping("/by-client/{clientId}")
    public List<BankAccountDto> byClient(@PathVariable("clientId") Long clientId) {
        return bankAccountService.getByClient(clientId);
    }

    @PostMapping("/{accountId}/close")
    public void close(@PathVariable("accountId") Long accountId) {
        bankAccountService.closeAccount(accountId);
    }

    @PostMapping("/{accountId}/deposit")
    public BankAccountDto deposit(@PathVariable("accountId") Long accountId, @Valid @RequestBody DepositRequestDto dto) {
        return bankAccountService.deposit(accountId, dto);
    }
}

