package com.example.bank.service;

import com.example.bank.model.CreditInterestSetting;
import com.example.bank.repository.CreditInterestSettingRepository;
import com.example.bank.service.InterestSettingService;
import com.example.bank.dto.CreditInterestSettingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import com.example.bank.exception.BusinessException;


@Service
@RequiredArgsConstructor
public class InterestSettingService {

    private final CreditInterestSettingRepository repository;


    public List<CreditInterestSettingDto> getAll() {
    return repository.findAll()
            .stream()
            .map(this::mapToDTO)
            .toList();
    }

    public CreditInterestSettingDto create(CreditInterestSettingDto dto) {
       CreditInterestSetting entity = mapToEntity(dto);

       validate(entity);

       boolean exists = repository.existsExactSetting(
               entity.getCreditType(),
               entity.getMinIncome(),
               entity.getMaxIncome()
       );

       if (exists) {
           throw new BusinessException("Вече съществува такава конфигурация");
       }

       return mapToDTO(repository.save(entity));
    }

public CreditInterestSettingDto update(Long id, CreditInterestSettingDto dto) {

    CreditInterestSetting existing = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Настройката не е намерена"));

    CreditInterestSetting updated = mapToEntity(dto);

    validate(updated);

    boolean exists = repository.existsExactSetting(
            updated.getCreditType(),
            updated.getMinIncome(),
            updated.getMaxIncome()
    );

    if (exists &&
        !(existing.getCreditType().equals(updated.getCreditType()) &&
          existing.getMinIncome().compareTo(updated.getMinIncome()) == 0 &&
          ((existing.getMaxIncome() == null && updated.getMaxIncome() == null) ||
           (existing.getMaxIncome() != null &&
            existing.getMaxIncome().compareTo(updated.getMaxIncome()) == 0)))
    ) {
        throw new BusinessException("Вече съществува такава конфигурация");
    }

    existing.setCreditType(updated.getCreditType());
    existing.setMinIncome(updated.getMinIncome());
    existing.setMaxIncome(updated.getMaxIncome());
    existing.setInterestRate(updated.getInterestRate());
    existing.setMaxDebtRatio(updated.getMaxDebtRatio());
    existing.setMinDownPaymentPct(updated.getMinDownPaymentPct());

    return mapToDTO(repository.save(existing));
}

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("Настройката не съществува");
        }
        repository.deleteById(id);
    }

    private void validate(CreditInterestSetting s) {

        if (s.getCreditType() == null) {
            throw new BusinessException("Типът кредит е задължителен");
        }

        if (s.getMinIncome() == null || s.getMinIncome().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Минималният доход трябва да е >= 0");
        }

        if (s.getMaxIncome() != null &&
                s.getMaxIncome().compareTo(s.getMinIncome()) < 0) {
            throw new BusinessException("Максималният доход не може да е по-малък от минималния");
        }

        if (s.getInterestRate() == null ||
                s.getInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Лихвата трябва да е положителна");
        }

        if (s.getMaxDebtRatio() == null ||
                s.getMaxDebtRatio().compareTo(BigDecimal.ZERO) <= 0 ||
                s.getMaxDebtRatio().compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException("Макс. дълг трябва да е между 0 и 1");
        }

        if (s.getCreditType().name().equals("MORTGAGE")) {
            if (s.getMinDownPaymentPct() == null ||
                    s.getMinDownPaymentPct().compareTo(BigDecimal.ZERO) <= 0 ||
                    s.getMinDownPaymentPct().compareTo(BigDecimal.ONE) > 0) {
                throw new BusinessException("Самоучастието трябва да е между 0 и 1");
            }
        }
    }

    private CreditInterestSetting mapToEntity(CreditInterestSettingDto dto) {
    CreditInterestSetting e = new CreditInterestSetting();
    e.setId(dto.getId());
    e.setCreditType(dto.getCreditType());
    e.setMinIncome(dto.getMinIncome());
    e.setMaxIncome(dto.getMaxIncome());
    e.setInterestRate(dto.getInterestRate());
    e.setMaxDebtRatio(dto.getMaxDebtRatio());
    e.setMinDownPaymentPct(dto.getMinDownPaymentPct());
    return e;
}

private CreditInterestSettingDto mapToDTO(CreditInterestSetting e) {
    CreditInterestSettingDto dto = new CreditInterestSettingDto();
    dto.setId(e.getId());
    dto.setCreditType(e.getCreditType());
    dto.setMinIncome(e.getMinIncome());
    dto.setMaxIncome(e.getMaxIncome());
    dto.setInterestRate(e.getInterestRate());
    dto.setMaxDebtRatio(e.getMaxDebtRatio());
    dto.setMinDownPaymentPct(e.getMinDownPaymentPct());
    return dto;
}
}