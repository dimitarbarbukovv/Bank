package com.example.bank.controller;

import com.example.bank.dto.ChangePasswordDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateProfileDto;
import com.example.bank.security.JwtService;
import com.example.bank.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsTokenAndRole() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin",
                "x",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken("admin", "ROLE_ADMIN")).thenReturn("jwt-token");

        ResponseEntity<Map<String, String>> response = authController.login(req);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwt-token", response.getBody().get("token"));
        assertEquals("ROLE_ADMIN", response.getBody().get("role"));
    }

    @Test
    void meDelegatesToEmployeeService() {
        EmployeeDto dto = new EmployeeDto();
        dto.setUsername("admin");
        when(employeeService.getCurrentProfile()).thenReturn(dto);

        EmployeeDto out = authController.me();
        assertEquals("admin", out.getUsername());
    }

    @Test
    void updateMeHandlesNullBody() {
        EmployeeDto dto = new EmployeeDto();
        dto.setUsername("u1");
        when(employeeService.updateCurrentProfile(null)).thenReturn(dto);

        EmployeeDto out = authController.updateMe(null);
        assertEquals("u1", out.getUsername());
    }

    @Test
    void changePasswordReturnsSuccessMessage() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setCurrentPassword("old");
        dto.setNewPassword("newpass");

        ResponseEntity<Map<String, String>> out = authController.changePassword(dto);
        assertEquals(200, out.getStatusCode().value());
        assertEquals("Паролата е сменена успешно", out.getBody().get("message"));
        verify(employeeService).changeOwnPassword("old", "newpass");
    }
}
