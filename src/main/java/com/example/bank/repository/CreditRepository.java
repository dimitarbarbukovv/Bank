package com.example.bank.repository;

import com.example.bank.model.Credit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    @EntityGraph(attributePaths = {"createdBy"})
    List<Credit> findByClientIdOrderByIdDesc(Long clientId);
}

