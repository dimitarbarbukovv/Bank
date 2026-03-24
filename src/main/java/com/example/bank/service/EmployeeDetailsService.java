package com.example.bank.service;

import com.example.bank.model.Employee;
import com.example.bank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee emp = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + emp.getRole().name());
        return new User(emp.getUsername(), emp.getPasswordHash(), emp.isActive(), true, true, true, List.of(authority));
    }
}

