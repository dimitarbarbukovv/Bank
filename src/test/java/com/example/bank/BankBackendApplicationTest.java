package com.example.bank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BankBackendApplicationTest {

    @Test
    void classHasSpringBootApplicationAnnotation() {
        assertTrue(BankBackendApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
