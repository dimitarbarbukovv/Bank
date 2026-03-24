package com.example.bank.config;

import com.example.bank.model.Employee;
import com.example.bank.model.EmployeeRole;
import com.example.bank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner initEmployees(EmployeeRepository employeeRepository) {
        return args -> {
            // Data migration for normalized client subtypes (idempotent).
            jdbcTemplate.execute("""
                create table if not exists individual_clients (
                    id bigint primary key references clients(id) on delete cascade,
                    first_name varchar(100) not null,
                    last_name varchar(100) not null,
                    egn varchar(10) not null unique
                )
            """);
            jdbcTemplate.execute("""
                create table if not exists company_clients (
                    id bigint primary key references clients(id) on delete cascade,
                    company_name varchar(255) not null,
                    eik varchar(13) not null unique,
                    representative_name varchar(200) not null
                )
            """);
            jdbcTemplate.execute("""
                insert into individual_clients (id, first_name, last_name, egn)
                select c.id, c.first_name, c.last_name, c.egn
                from clients c
                where c.type = 'INDIVIDUAL'
                  and c.egn is not null
                  and not exists (select 1 from individual_clients i where i.id = c.id)
            """);
            jdbcTemplate.execute("""
                insert into company_clients (id, company_name, eik, representative_name)
                select c.id, c.company_name, c.eik, c.representative_name
                from clients c
                where c.type = 'COMPANY'
                  and c.eik is not null
                  and not exists (select 1 from company_clients cc where cc.id = c.id)
            """);
            jdbcTemplate.execute("""
                create unique index if not exists uk_installments_credit_month
                on installments(credit_id, month_number)
            """);

            if (employeeRepository.count() == 0) {
                Employee admin = new Employee();
                admin.setUsername("admin");
                admin.setDisplayName("Администратор");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setRole(EmployeeRole.ADMIN);
                employeeRepository.save(admin);

                Employee employee = new Employee();
                employee.setUsername("employee");
                employee.setDisplayName("Служител (демо)");
                employee.setPasswordHash(passwordEncoder.encode("emp123"));
                employee.setRole(EmployeeRole.EMPLOYEE);
                employeeRepository.save(employee);
            }
        };
    }
}

