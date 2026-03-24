package com.example.bank.service;

import com.example.bank.exception.NotFoundException;
import com.example.bank.model.Employee;
import com.example.bank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final EmployeeRepository employeeRepository;

    public Employee requireCurrentEmployee() {
        return getCurrentEmployee()
                .orElseThrow(() -> new NotFoundException("Служителят не е намерен"));
    }

    public Optional<Employee> getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String username = auth.getName();
        return employeeRepository.findByUsername(username);
    }
}
