package com.example.bank.security;

import com.example.bank.service.EmployeeDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import jakarta.servlet.FilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private EmployeeDetailsService employeeDetailsService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsWhenNoBearerHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(req, res, filterChain);
        verify(filterChain).doFilter(req, res);
        verifyNoInteractions(jwtService);
    }

    @Test
    void skipsWhenTokenParsingFails() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad");
        MockHttpServletResponse res = new MockHttpServletResponse();
        when(jwtService.extractUsername("bad")).thenThrow(new RuntimeException("bad token"));

        jwtAuthFilter.doFilterInternal(req, res, filterChain);
        verify(filterChain).doFilter(req, res);
    }

    @Test
    void setsAuthenticationForValidToken() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtService.extractUsername("good")).thenReturn("admin");
        when(employeeDetailsService.loadUserByUsername("admin")).thenReturn(
                new User("admin", "x", List.of())
        );

        jwtAuthFilter.doFilterInternal(req, res, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(req, res);
    }
}
