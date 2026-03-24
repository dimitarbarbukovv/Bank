package com.example.bank.repository;

import com.example.bank.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByCreditIdOrderByMonthNumberAsc(Long creditId);
}

