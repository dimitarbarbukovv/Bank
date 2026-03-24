package com.example.bank.security;

import com.example.bank.service.EmployeeDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void createsAuthenticationProviderAndPasswordEncoder() {
        EmployeeDetailsService uds = mock(EmployeeDetailsService.class);
        JwtAuthFilter filter = mock(JwtAuthFilter.class);
        SecurityConfig cfg = new SecurityConfig(uds, filter);

        PasswordEncoder encoder = cfg.passwordEncoder();
        AuthenticationProvider provider = cfg.authenticationProvider();

        assertNotNull(encoder);
        assertNotNull(provider);
    }

    @Test
    void returnsAuthenticationManagerFromConfiguration() throws Exception {
        EmployeeDetailsService uds = mock(EmployeeDetailsService.class);
        JwtAuthFilter filter = mock(JwtAuthFilter.class);
        SecurityConfig cfg = new SecurityConfig(uds, filter);

        AuthenticationConfiguration ac = mock(AuthenticationConfiguration.class);
        AuthenticationManager am = mock(AuthenticationManager.class);
        when(ac.getAuthenticationManager()).thenReturn(am);

        assertSame(am, cfg.authenticationManager(ac));
    }

    @Test
    void corsConfigurationContainsExpectedOriginsAndMethods() {
        EmployeeDetailsService uds = mock(EmployeeDetailsService.class);
        JwtAuthFilter filter = mock(JwtAuthFilter.class);
        SecurityConfig cfg = new SecurityConfig(uds, filter);

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/clients");
        request.addHeader("Origin", "http://127.0.0.1:5173");
        request.addHeader("Access-Control-Request-Method", "GET");
        CorsConfiguration cc = source.getCorsConfiguration(request);
        assertNotNull(cc);
        assertTrue(cc.getAllowedOriginPatterns().contains("http://localhost:*"));
        assertTrue(cc.getAllowedOriginPatterns().contains("http://127.0.0.1:*"));
        assertTrue(cc.getAllowedMethods().contains("GET"));
        assertTrue(Boolean.TRUE.equals(cc.getAllowCredentials()));
    }
}
