package com.example.bank.controller;

import com.example.bank.dto.CreditRequestDto;
import com.example.bank.dto.CreditSummaryDto;
import com.example.bank.dto.InstallmentDto;
import com.example.bank.model.CreditType;
import com.example.bank.service.CreditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditControllerTest {

    @Mock
    private CreditService creditService;

    @InjectMocks
    private CreditController creditController;

    @Test
    void createReturnsIdMap() {
        CreditRequestDto dto = new CreditRequestDto();
        when(creditService.createCredit(dto)).thenReturn(42L);
        Map<String, Long> out = creditController.create(dto);
        assertEquals(42L, out.get("id"));
    }

    @Test
    void scheduleDelegates() {
        when(creditService.getSchedule(2L)).thenReturn(List.of(new InstallmentDto()));
        assertEquals(1, creditController.schedule(2L).size());
    }

    @Test
    void payDelegates() {
        creditController.pay(5L);
        verify(creditService).markInstallmentPaid(5L);
    }

    @Test
    void statusReturnsMap() {
        when(creditService.getCreditStatus(7L)).thenReturn("NEW");
        assertEquals("NEW", creditController.status(7L).get("status"));
    }

    @Test
    void byClientDelegates() {
        when(creditService.getByClient(3L)).thenReturn(List.of(new CreditSummaryDto()));
        assertEquals(1, creditController.byClient(3L).size());
    }

    @Test
    void suggestionConvertsTypeAndReturnsValue() {
        when(creditService.calculateSuggestedMaxPrincipal(
                CreditType.CONSUMER,
                BigDecimal.valueOf(3000),
                60,
                null,
                null
        )).thenReturn(BigDecimal.valueOf(15000));

        Map<String, BigDecimal> out = creditController.suggestion(
                "CONSUMER",
                BigDecimal.valueOf(3000),
                60,
                null,
                null
        );
        assertEquals(BigDecimal.valueOf(15000), out.get("maxPrincipal"));
    }
}
