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
    public CommandLineRunner init(EmployeeRepository employeeRepository) {
        return args -> {
            initSchema();
            initEmployees(employeeRepository);
            initSettings();
        };
    }

    public void initSchema() {

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
            create unique index if not exists uk_installments_credit_month
            on installments(credit_id, month_number)
        """);

        jdbcTemplate.execute("""
            create table if not exists credit_interest_setting (
                id bigserial primary key,
                credit_type varchar(20) not null,
                min_income double precision not null,
                max_income double precision,
                interest_rate double precision not null,
                max_debt_ratio double precision not null,
                min_down_payment_pct double precision
            )
        """);

        jdbcTemplate.execute("""
            alter table credit_interest_setting
            add column if not exists max_debt_ratio double precision default 0.30
        """);

        jdbcTemplate.execute("""
            alter table credit_interest_setting
            add column if not exists min_down_payment_pct double precision
        """);
    }

    public void initEmployees(EmployeeRepository employeeRepository) {
        if (employeeRepository.count() > 0) return;

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

    public void initSettings() {

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from credit_interest_setting",
                Integer.class
        );

        if (count != null && count > 0) return;

        // CONSUMER
        insertSetting("CONSUMER", 0, 1000, 12.5, 0.30, null);
        insertSetting("CONSUMER", 1000, 3000, 10.0, 0.30, null);
        insertSetting("CONSUMER", 3000, null, 8.5, 0.30, null);

        // MORTGAGE
        insertSetting("MORTGAGE", 0, 2000, 6.5, 0.30, 0.20);
        insertSetting("MORTGAGE", 2000, 5000, 5.5, 0.30, 0.20);
        insertSetting("MORTGAGE", 5000, null, 4.5, 0.30, 0.20);
    }

    private void insertSetting(String type,
                               Integer minIncome,
                               Integer maxIncome,
                               double interest,
                               double debtRatio,
                               Double downPaymentPct) {

        jdbcTemplate.update("""
            insert into credit_interest_setting
            (credit_type, min_income, max_income, interest_rate, max_debt_ratio, min_down_payment_pct)
            values (?, ?, ?, ?, ?, ?)
        """,
                type,
                minIncome,
                maxIncome,
                interest,
                debtRatio,
                downPaymentPct
        );
    }
}