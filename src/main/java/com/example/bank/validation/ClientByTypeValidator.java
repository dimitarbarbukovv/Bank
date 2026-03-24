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
            boolean ok = hasText(dto.getFirstName()) && hasText(dto.getLastName()) && hasText(dto.getEgn());
            if (!ok) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("За физическо лице са задължителни име, фамилия и ЕГН")
                        .addConstraintViolation();
            }
            return ok;
        }

        boolean ok = hasText(dto.getCompanyName()) && hasText(dto.getEik()) && hasText(dto.getRepresentativeName());
        if (!ok) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("За юридическо лице са задължителни фирма, ЕИК и представител")
                    .addConstraintViolation();
        }
        return ok;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
