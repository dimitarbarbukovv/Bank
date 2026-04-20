package com.example.bank.controller;

import com.example.bank.dto.CreditInterestSettingDto;
import com.example.bank.service.InterestSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings/interest")
@RequiredArgsConstructor
public class InterestSettingController {

    private final InterestSettingService service;

    @GetMapping
    public List<CreditInterestSettingDto> getAll() {
        return service.getAll();
    }

    @PostMapping
    public CreditInterestSettingDto create(@RequestBody CreditInterestSettingDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public CreditInterestSettingDto update(@PathVariable Long id,
                                           @RequestBody CreditInterestSettingDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}