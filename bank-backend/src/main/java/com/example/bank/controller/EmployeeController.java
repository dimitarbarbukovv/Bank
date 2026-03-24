package com.example.bank.controller;

import com.example.bank.dto.CreateEmployeeDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateEmployeeDto;
import com.example.bank.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmployeeDto> list() {
        return employeeService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDto create(@Valid @RequestBody CreateEmployeeDto dto) {
        return employeeService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeDto dto) {
        return employeeService.update(id, dto);
    }
}
