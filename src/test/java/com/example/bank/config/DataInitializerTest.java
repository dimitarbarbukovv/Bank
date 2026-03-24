package com.example.bank.config;

import com.example.bank.model.Employee;
import com.example.bank.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Test
    void seedsDefaultUsersWhenRepositoryEmpty() throws Exception {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        EmployeeRepository repo = mock(EmployeeRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(repo.count()).thenReturn(0L);
        when(encoder.encode(any())).thenReturn("hash");

        DataInitializer initializer = new DataInitializer(encoder, jdbcTemplate);
        CommandLineRunner runner = initializer.initEmployees(repo);

        assertDoesNotThrow(() -> runner.run());
        verify(repo, times(2)).save(any(Employee.class));
    }

    @Test
    void doesNotSeedWhenUsersExist() throws Exception {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        EmployeeRepository repo = mock(EmployeeRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(repo.count()).thenReturn(2L);

        DataInitializer initializer = new DataInitializer(encoder, jdbcTemplate);
        initializer.initEmployees(repo).run();

        verify(repo, never()).save(any(Employee.class));
    }
}
