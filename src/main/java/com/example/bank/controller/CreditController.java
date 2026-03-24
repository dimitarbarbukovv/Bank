package com.example.bank.controller;

import com.example.bank.dto.CreditRequestDto;
import com.example.bank.dto.CreditSummaryDto;
import com.example.bank.dto.InstallmentDto;
import com.example.bank.model.CreditType;
import com.example.bank.service.CreditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/credits")
@CrossOrigin(origins = "http://localhost:5173")
public class CreditController {

    private CreditService creditService;

    @Autowired
    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping
    public Map<String, Long> create(@Valid @RequestBody CreditRequestDto dto) {
        Long id = creditService.createCredit(dto);
        return Map.of("id", id);
    }

    @GetMapping("/{creditId}/schedule")
    public List<InstallmentDto> schedule(@PathVariable("creditId") Long creditId) {
        return creditService.getSchedule(creditId);
    }

    @PostMapping("/installments/{installmentId}/pay")
    public void pay(@PathVariable("installmentId") Long installmentId) {
        creditService.markInstallmentPaid(installmentId);
    }

    @GetMapping("/{creditId}/status")
    public Map<String, String> status(@PathVariable("creditId") Long creditId) {
        return Map.of("status", creditService.getCreditStatus(creditId));
    }

    @GetMapping("/by-client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<CreditSummaryDto> byClient(@PathVariable("clientId") Long clientId) {
        return creditService.getByClient(clientId);
    }

    @GetMapping("/suggestion")
    public Map<String, BigDecimal> suggestion(@RequestParam("type") String type,
                                              @RequestParam("netIncome") BigDecimal netIncome,
                                              @RequestParam("termMonths") Integer termMonths,
                                              @RequestParam(value = "propertyValue", required = false) BigDecimal propertyValue,
                                              @RequestParam(value = "downPayment", required = false) BigDecimal downPayment) {
        BigDecimal max = creditService.calculateSuggestedMaxPrincipal(
                CreditType.valueOf(type), netIncome, termMonths, propertyValue, downPayment
        );
        return Map.of("maxPrincipal", max);
    }
}

