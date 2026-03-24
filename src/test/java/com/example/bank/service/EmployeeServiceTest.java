package com.example.bank.service;

import com.example.bank.dto.CreateEmployeeDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateEmployeeDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.exception.NotFoundException;
import com.example.bank.mapper.EmployeeMapper;
import com.example.bank.model.Employee;
import com.example.bank.model.EmployeeRole;
import com.example.bank.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUserService currentUserService;
    @Spy
    private EmployeeMapper employeeMapper = new EmployeeMapper();

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployeeSuccess() {
        CreateEmployeeDto dto = new CreateEmployeeDto();
        dto.setUsername("newuser");
        dto.setPassword("secret123");
        dto.setRole(EmployeeRole.EMPLOYEE);
        dto.setDisplayName(" New User ");

        when(employeeRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> {
            Employee e = i.getArgument(0);
            e.setId(8L);
            return e;
        });

        EmployeeDto out = employeeService.create(dto);

        assertEquals(8L, out.getId());
        assertEquals("newuser", out.getUsername());
        assertEquals("New User", out.getDisplayName());
        assertTrue(out.isActive());
    }

    @Test
    void createDuplicateUsernameThrows() {
        CreateEmployeeDto dto = new CreateEmployeeDto();
        dto.setUsername("admin");
        when(employeeRepository.findByUsername("admin")).thenReturn(Optional.of(new Employee()));

        assertThrows(BusinessException.class, () -> employeeService.create(dto));
    }

    @Test
    void updateMissingEmployeeThrows() {
        when(employeeRepository.findById(22L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> employeeService.update(22L, new UpdateEmployeeDto()));
    }

    @Test
    void updateWithProvidedNewPasswordEncodesIt() {
        Employee e = new Employee();
        e.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));
        when(passwordEncoder.encode("123")).thenReturn("hash123");

        UpdateEmployeeDto dto = new UpdateEmployeeDto();
        dto.setNewPassword("123");

        EmployeeDto out = employeeService.update(1L, dto);
        assertNotNull(out);
        assertEquals("hash123", e.getPasswordHash());
    }

    @Test
    void changeOwnPasswordChecksCurrentAndEncodesNew() {
        Employee me = new Employee();
        me.setPasswordHash("oldHash");
        when(currentUserService.requireCurrentEmployee()).thenReturn(me);
        when(passwordEncoder.matches("oldPass", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");

        employeeService.changeOwnPassword("oldPass", "newPass");

        assertEquals("newHash", me.getPasswordHash());
    }

    @Test
    void changeOwnPasswordWrongCurrentThrows() {
        Employee me = new Employee();
        me.setPasswordHash("oldHash");
        when(currentUserService.requireCurrentEmployee()).thenReturn(me);
        when(passwordEncoder.matches("bad", "oldHash")).thenReturn(false);

        assertThrows(BusinessException.class, () -> employeeService.changeOwnPassword("bad", "newPass"));
    }
}
