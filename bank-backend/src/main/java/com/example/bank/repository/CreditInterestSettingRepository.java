package com.example.bank.repository;

import com.example.bank.model.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CreditInterestSettingRepository extends JpaRepository<CreditInterestSetting, Long> {

        @Query("""
            SELECT s FROM CreditInterestSetting s
            WHERE s.creditType = :type
            AND :income >= s.minIncome
            AND (:income < s.maxIncome OR s.maxIncome IS NULL)
        """)
        Optional<CreditInterestSetting> findMatching(CreditType type, BigDecimal income);
    
        List<CreditInterestSetting> findByCreditType(CreditType creditType);


        @Query("""
        SELECT COUNT(s) > 0 FROM CreditInterestSetting s
        WHERE s.creditType = :creditType
        AND s.minIncome = :minIncome
        AND (
            (s.maxIncome IS NULL AND :maxIncome IS NULL)
            OR s.maxIncome = :maxIncome
        )
        """)
        boolean existsExactSetting(
                CreditType creditType,
                BigDecimal minIncome,
                BigDecimal maxIncome
        );
}