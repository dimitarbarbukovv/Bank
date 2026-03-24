package com.example.bank.service;

import com.example.bank.dto.BankAccountDto;
import com.example.bank.dto.DepositRequestDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.exception.NotFoundException;
import com.example.bank.mapper.BankAccountMapper;
import com.example.bank.model.AccountCurrency;
import com.example.bank.model.AccountStatus;
import com.example.bank.model.BankAccount;
import com.example.bank.model.Client;
import com.example.bank.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final ClientService clientService;
    private final CurrentUserService currentUserService;
    private final BankAccountMapper bankAccountMapper;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public BankAccountDto openAccount(BankAccountDto dto) {
        Client owner = clientService.getById(dto.getClientId());
        if (dto.getCurrency() == AccountCurrency.BGN) {
            throw new BusinessException("Валута BGN вече не се поддържа за нови сметки");
        }

        BankAccount account = new BankAccount();
        account.setIban(generateBulgarianIban());
        account.setOwner(owner);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : AccountCurrency.EUR);
        account.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        currentUserService.getCurrentEmployee().ifPresent(account::setCreatedBy);

        account = bankAccountRepository.save(account);
        return bankAccountMapper.toDto(account);
    }

    private String generateBulgarianIban() {
        // Country code BG + 2 check digits + bank code + 14 digits
        String bankCode = "BANK"; // demo BIC-like code
        while (true) {
            StringBuilder bban = new StringBuilder(bankCode);
            for (int i = 0; i < 14; i++) {
                bban.append(random.nextInt(10));
            }
            String iban = "BG00" + bban;
            // ensure uniqueness
            if (bankAccountRepository.findByIban(iban).isEmpty()) {
                return iban;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BankAccountDto> getByClient(Long clientId) {
        clientService.getById(clientId);
        return bankAccountRepository.findByOwner_IdOrderByIdDesc(clientId).stream()
                .map(bankAccountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void closeAccount(Long accountId) {
        BankAccount acc = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Сметката не е намерена"));
        if (acc.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Сметката може да се закрие само при нулева наличност");
        }
        acc.setStatus(AccountStatus.CLOSED);
    }

    @Transactional
    public BankAccountDto deposit(Long accountId, DepositRequestDto dto) {
        BankAccount acc = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Сметката не е намерена"));
        if (acc.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Не може да внасяте сума по закрита сметка");
        }
        acc.setBalance(acc.getBalance().add(dto.getAmount()));

        return bankAccountMapper.toDto(acc);
    }
}

