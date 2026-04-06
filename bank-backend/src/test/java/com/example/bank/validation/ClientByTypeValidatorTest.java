package com.example.bank.validation;

import com.example.bank.dto.ClientDto;
import com.example.bank.model.ClientType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientByTypeValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void individualValid_passes() {
        ClientDto dto = individual("Иванко", "Петровски", "0123456789");
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void individualBlankFirstName_fails() {
        ClientDto dto = individual(" ", "Петровски", "0123456789");
        assertViolationContains(dto, "firstName", "Името е задължително");
    }

    @Test
    void individualShortFirstName_fails() {
        ClientDto dto = individual("Ана", "Петровски", "0123456789");
        assertViolationContains(dto, "firstName", "поне 4 символа");
    }

    @Test
    void individualBlankLastName_fails() {
        ClientDto dto = individual("Иванко", "  ", "0123456789");
        assertViolationContains(dto, "lastName", "Фамилията е задължителна");
    }

    @Test
    void individualShortLastName_fails() {
        ClientDto dto = individual("Иванко", "Ана", "0123456789");
        assertViolationContains(dto, "lastName", "поне 4 символа");
    }

    @Test
    void individualBlankEgn_fails() {
        ClientDto dto = individual("Иванко", "Петровски", "  ");
        assertViolationContains(dto, "egn", "ЕГН е задължително");
    }

    @Test
    void individualEgnNonDigits_fails() {
        ClientDto dto = individual("Иванко", "Петровски", "012345678a");
        assertViolationContains(dto, "egn", "точно 10 цифри");
    }

    @Test
    void individualEgnWrongLength_fails() {
        ClientDto dto = individual("Иванко", "Петровски", "12345");
        assertViolationContains(dto, "egn", "точно 10 цифри");
    }

    @Test
    void companyValid_passes() {
        ClientDto dto = company("АОББ АД", "1234567890", "Георги Георгиев");
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void companyBlankName_fails() {
        ClientDto dto = company("  ", "1234567890", "Георги Георгиев");
        assertViolationContains(dto, "companyName", "фирмата е задължително");
    }

    @Test
    void companyBlankRepresentative_fails() {
        ClientDto dto = company("АОББ АД", "1234567890", " ");
        assertViolationContains(dto, "representativeName", "представителя е задължително");
    }

    @Test
    void companyBlankEik_fails() {
        ClientDto dto = company("АОББ АД", "  ", "Георги Георгиев");
        assertViolationContains(dto, "eik", "ЕИК е задължителен");
    }

    @Test
    void companyEikNotTenDigits_fails() {
        ClientDto dto = company("АОББ АД", "12345678901", "Георги Георгиев");
        assertViolationContains(dto, "eik", "точно 10 цифри");
    }

    @Test
    void companyEikLetters_fails() {
        ClientDto dto = company("АОББ АД", "123456789a", "Георги Георгиев");
        assertViolationContains(dto, "eik", "точно 10 цифри");
    }

    private static ClientDto individual(String first, String last, String egn) {
        ClientDto dto = new ClientDto();
        dto.setType(ClientType.INDIVIDUAL);
        dto.setFirstName(first);
        dto.setLastName(last);
        dto.setEgn(egn);
        return dto;
    }

    private static ClientDto company(String name, String eik, String rep) {
        ClientDto dto = new ClientDto();
        dto.setType(ClientType.COMPANY);
        dto.setCompanyName(name);
        dto.setEik(eik);
        dto.setRepresentativeName(rep);
        return dto;
    }

    private void assertViolationContains(ClientDto dto, String property, String messagePart) {
        Set<ConstraintViolation<ClientDto>> violations = validator.validate(dto);
        assertTrue(!violations.isEmpty(), "expected violations");
        String props = violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.joining(","));
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals(property) && v.getMessage().contains(messagePart)),
                "property=" + property + " part=" + messagePart + " got paths: " + props);
    }
}
