package com.example.bank.mapper;

import com.example.bank.dto.CreateEmployeeDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateEmployeeDto;
import com.example.bank.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee fromCreateDto(CreateEmployeeDto dto, String passwordHash) {
        Employee e = new Employee();
        e.setUsername(trim(dto.getUsername()));
        e.setPasswordHash(passwordHash);
        e.setRole(dto.getRole());
        e.setDisplayName(trimToNull(dto.getDisplayName()));
        e.setActive(true);
        return e;
    }

    public void updateFromDto(Employee e, UpdateEmployeeDto dto) {
        if (dto.getDisplayName() != null) {
            e.setDisplayName(trimToNull(dto.getDisplayName()));
        }
        if (dto.getRole() != null) {
            e.setRole(dto.getRole());
        }
        if (dto.getActive() != null) {
            e.setActive(dto.getActive());
        }
    }

    public EmployeeDto toDto(Employee e) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        dto.setUsername(e.getUsername());
        dto.setDisplayName(e.getDisplayName());
        dto.setRole(e.getRole());
        dto.setActive(e.isActive());
        return dto;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
