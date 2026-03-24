package com.example.bank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ClientByTypeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidClientByType {
    String message() default "Невалидни данни за типа клиент";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
