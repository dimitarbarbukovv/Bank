package com.example.bank.service;

import com.example.bank.dto.CreateEmployeeDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateEmployeeDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.exception.NotFoundException;
import com.example.bank.mapper.EmployeeMapper;
import com.example.bank.model.Employee;
import com.example.bank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public List<EmployeeDto> listAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDto create(CreateEmployeeDto dto) {
        if (employeeRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("Вече съществува служител с това потребителско име");
        }
        Employee e = employeeMapper.fromCreateDto(dto, passwordEncoder.encode(dto.getPassword()));
        return employeeMapper.toDto(employeeRepository.save(e));
    }

    @Transactional
    public EmployeeDto update(Long id, UpdateEmployeeDto dto) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Служителят не е намерен"));
        employeeMapper.updateFromDto(e, dto);
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            e.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        return employeeMapper.toDto(e);
    }

    @Transactional
    public void changeOwnPassword(String currentPassword, String newPassword) {
        Employee me = currentUserService.requireCurrentEmployee();
        if (!passwordEncoder.matches(currentPassword, me.getPasswordHash())) {
            throw new BusinessException("Текущата парола не е верна");
        }
        me.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    @Transactional(readOnly = true)
    public EmployeeDto getCurrentProfile() {
        return employeeMapper.toDto(currentUserService.requireCurrentEmployee());
    }

    @Transactional
    public EmployeeDto updateCurrentProfile(String displayName) {
        Employee me = currentUserService.requireCurrentEmployee();
        me.setDisplayName(displayName != null && !displayName.isBlank() ? displayName.trim() : null);
        return employeeMapper.toDto(me);
    }
}
