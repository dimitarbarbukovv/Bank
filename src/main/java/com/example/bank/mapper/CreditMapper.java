package com.example.bank.mapper;

import com.example.bank.dto.CreditSummaryDto;
import com.example.bank.dto.InstallmentDto;
import com.example.bank.model.Credit;
import com.example.bank.model.Employee;
import com.example.bank.model.Installment;
import org.springframework.stereotype.Component;

@Component
public class CreditMapper {

    public CreditSummaryDto toSummaryDto(Credit c, String status) {
        CreditSummaryDto dto = new CreditSummaryDto();
        dto.setId(c.getId());
        dto.setType(c.getType());
        dto.setPrincipal(c.getPrincipal());
        dto.setTermMonths(c.getTermMonths());
        dto.setAnnualInterestRate(c.getAnnualInterestRate());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setStatus(status);
        Employee e = c.getCreatedBy();
        if (e != null) {
            dto.setCreatedByUsername(e.getUsername());
            dto.setCreatedByDisplayName(resolveEmployeeDisplay(e));
        }
        return dto;
    }

    public InstallmentDto toInstallmentDto(Installment inst) {
        InstallmentDto dto = new InstallmentDto();
        dto.setId(inst.getId());
        dto.setMonthNumber(inst.getMonthNumber());
        dto.setPaymentAmount(inst.getPaymentAmount());
        dto.setPrincipalPart(inst.getPrincipalPart());
        dto.setInterestPart(inst.getInterestPart());
        dto.setRemainingPrincipal(inst.getRemainingPrincipal());
        dto.setPaid(inst.isPaid());
        return dto;
    }

    private static String resolveEmployeeDisplay(Employee e) {
        if (e.getDisplayName() != null && !e.getDisplayName().isBlank()) {
            return e.getDisplayName();
        }
        return e.getUsername();
    }
}
