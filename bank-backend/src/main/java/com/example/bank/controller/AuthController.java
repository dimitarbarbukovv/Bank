package com.example.bank.controller;

import com.example.bank.dto.ChangePasswordDto;
import com.example.bank.dto.EmployeeDto;
import com.example.bank.dto.UpdateProfileDto;
import com.example.bank.security.JwtService;
import com.example.bank.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployeeService employeeService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String role = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_EMPLOYEE");
        String jwt = jwtService.generateToken(request.getUsername(), role);
        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "role", role
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public EmployeeDto me() {
        return employeeService.getCurrentProfile();
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public EmployeeDto updateMe(@Valid @RequestBody UpdateProfileDto dto) {
        return employeeService.updateCurrentProfile(dto != null ? dto.getDisplayName() : null);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        employeeService.changeOwnPassword(dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Паролата е сменена успешно"));
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
}

