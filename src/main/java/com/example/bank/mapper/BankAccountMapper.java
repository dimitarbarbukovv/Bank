package com.example.bank.mapper;

import com.example.bank.dto.BankAccountDto;
import com.example.bank.model.AccountCurrency;
import com.example.bank.model.BankAccount;
import com.example.bank.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class BankAccountMapper {

    public BankAccountDto toDto(BankAccount a) {
        BankAccountDto dto = new BankAccountDto();
        dto.setId(a.getId());
        dto.setIban(a.getIban());
        dto.setBalance(a.getBalance());
        dto.setStatus(a.getStatus());
        dto.setCurrency(a.getCurrency() != null ? a.getCurrency() : AccountCurrency.EUR);
        dto.setClientId(a.getOwner() != null ? a.getOwner().getId() : null);
        fillAudit(dto, a.getCreatedBy());
        return dto;
    }

    public void fillAudit(BankAccountDto dto, Employee e) {
        if (e != null) {
            dto.setCreatedByUsername(e.getUsername());
            dto.setCreatedByDisplayName(resolveDisplay(e));
        }
    }

    private static String resolveDisplay(Employee e) {
        if (e.getDisplayName() != null && !e.getDisplayName().isBlank()) {
            return e.getDisplayName();
        }
        return e.getUsername();
    }
}
