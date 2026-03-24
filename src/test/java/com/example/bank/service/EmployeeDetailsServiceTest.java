package com.example.bank.service;

import com.example.bank.model.Employee;
import com.example.bank.model.EmployeeRole;
import com.example.bank.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeDetailsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeDetailsService employeeDetailsService;

    @Test
    void loadUserByUsernameReturnsSpringUserDetails() {
        Employee e = new Employee();
        e.setUsername("employee");
        e.setPasswordHash("hash");
        e.setRole(EmployeeRole.EMPLOYEE);
        e.setActive(true);
        when(employeeRepository.findByUsername("employee")).thenReturn(Optional.of(e));

        var out = employeeDetailsService.loadUserByUsername("employee");
        assertEquals("employee", out.getUsername());
        assertEquals("hash", out.getPassword());
        assertTrue(out.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    void loadUserByUsernameMissingThrows() {
        when(employeeRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> employeeDetailsService.loadUserByUsername("missing"));
    }
}
