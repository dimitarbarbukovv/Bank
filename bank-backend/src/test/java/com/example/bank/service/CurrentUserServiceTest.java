package com.example.bank.service;

import com.example.bank.exception.NotFoundException;
import com.example.bank.model.Employee;
import com.example.bank.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentEmployeeReturnsEmptyForAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", "", java.util.List.of())
        );
        assertTrue(currentUserService.getCurrentEmployee().isEmpty());
    }

    @Test
    void getCurrentEmployeeReturnsEmployeeForAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "x", java.util.List.of())
        );
        Employee e = new Employee();
        e.setUsername("admin");
        when(employeeRepository.findByUsername("admin")).thenReturn(Optional.of(e));

        Optional<Employee> out = currentUserService.getCurrentEmployee();
        assertTrue(out.isPresent());
        assertEquals("admin", out.get().getUsername());
    }

    @Test
    void requireCurrentEmployeeThrowsWhenMissing() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "x", java.util.List.of())
        );
        when(employeeRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> currentUserService.requireCurrentEmployee());
    }
}
