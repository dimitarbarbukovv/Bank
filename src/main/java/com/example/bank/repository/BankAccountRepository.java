package com.example.bank.repository;

import com.example.bank.model.BankAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByIban(String iban);

    @EntityGraph(attributePaths = {"createdBy"})
    List<BankAccount> findByOwner_IdOrderByIdDesc(Long ownerId);
}

