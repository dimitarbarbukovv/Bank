package com.example.bank.validation;

import com.example.bank.dto.ClientDto;
import com.example.bank.model.ClientType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ClientByTypeValidator implements ConstraintValidator<ValidClientByType, ClientDto> {

    @Override
    public boolean isValid(ClientDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getType() == null) {
            return true;
        }

        if (dto.getType() == ClientType.INDIVIDUAL) {
            return validateIndividual(dto, context);
        }

        return validateCompany(dto, context);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean validateIndividual(ClientDto dto, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean ok = true;

        if (!hasText(dto.getFirstName())) {
            context.buildConstraintViolationWithTemplate("Името е задължително")
                    .addPropertyNode("firstName")
                    .addConstraintViolation();
            ok = false;
        } else if (dto.getFirstName().trim().length() <= 3) {
            context.buildConstraintViolationWithTemplate("Името трябва да е поне 4 символа (повече от 3)")
                    .addPropertyNode("firstName")
                    .addConstraintViolation();
            ok = false;
        }

        if (!hasText(dto.getLastName())) {
            context.buildConstraintViolationWithTemplate("Фамилията е задължителна")
                    .addPropertyNode("lastName")
                    .addConstraintViolation();
            ok = false;
        } else if (dto.getLastName().trim().length() <= 3) {
            context.buildConstraintViolationWithTemplate("Фамилията трябва да е поне 4 символа (повече от 3)")
                    .addPropertyNode("lastName")
                    .addConstraintViolation();
            ok = false;
        }

        if (!hasText(dto.getEgn())) {
            context.buildConstraintViolationWithTemplate("ЕГН е задължително")
                    .addPropertyNode("egn")
                    .addConstraintViolation();
            ok = false;
        } else {
            String egn = dto.getEgn().trim();
            if (!egn.matches("\\d{10}")) {
                context.buildConstraintViolationWithTemplate("ЕГН трябва да съдържа точно 10 цифри")
                        .addPropertyNode("egn")
                        .addConstraintViolation();
                ok = false;
            }
        }

        return ok;
    }

    private static boolean validateCompany(ClientDto dto, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean ok = true;

        if (!hasText(dto.getCompanyName())) {
            context.buildConstraintViolationWithTemplate("Името на фирмата е задължително")
                    .addPropertyNode("companyName")
                    .addConstraintViolation();
            ok = false;
        }

        if (!hasText(dto.getRepresentativeName())) {
            context.buildConstraintViolationWithTemplate("Името на представителя е задължително")
                    .addPropertyNode("representativeName")
                    .addConstraintViolation();
            ok = false;
        }

        if (!hasText(dto.getEik())) {
            context.buildConstraintViolationWithTemplate("ЕИК е задължителен")
                    .addPropertyNode("eik")
                    .addConstraintViolation();
            ok = false;
        } else {
            String eik = dto.getEik().trim();
            if (!eik.matches("\\d{10}")) {
                context.buildConstraintViolationWithTemplate("ЕИК трябва да съдържа точно 10 цифри")
                        .addPropertyNode("eik")
                        .addConstraintViolation();
                ok = false;
            }
        }

        return ok;
    }
}
