package com.example.bank.controller;

import com.example.bank.dto.CreateEmployeeDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateEmployeeDto;
import com.example.bank.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @Test
    void listDelegates() {
        when(employeeService.listAll()).thenReturn(List.of(new EmployeeDto()));
        assertEquals(1, employeeController.list().size());
    }

    @Test
    void createDelegates() {
        CreateEmployeeDto in = new CreateEmployeeDto();
        EmployeeDto out = new EmployeeDto();
        out.setUsername("u1");
        when(employeeService.create(in)).thenReturn(out);
        assertEquals("u1", employeeController.create(in).getUsername());
    }

    @Test
    void updateDelegates() {
        UpdateEmployeeDto in = new UpdateEmployeeDto();
        EmployeeDto out = new EmployeeDto();
        out.setId(7L);
        when(employeeService.update(7L, in)).thenReturn(out);
        assertEquals(7L, employeeController.update(7L, in).getId());
    }
}
