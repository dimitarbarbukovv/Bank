package com.example.bank.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void handleNotFoundReturns404() {
        ResponseEntity<Map<String, Object>> out = handler.handleNotFound(
                new NotFoundException("missing"),
                new MockHttpServletRequest()
        );
        assertEquals(404, out.getStatusCode().value());
        assertEquals("missing", out.getBody().get("error"));
    }

    @Test
    void handleBusinessReturns400() {
        ResponseEntity<Map<String, Object>> out = handler.handleBusiness(
                new BusinessException("bad"),
                new MockHttpServletRequest()
        );
        assertEquals(400, out.getStatusCode().value());
        assertEquals("bad", out.getBody().get("error"));
    }

    @Test
    void handleValidationTranslatesMessages() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj", "egn", "size must be between 10 and 10"));
        Method method = Dummy.class.getMethod("setValue", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<Map<String, Object>> out = handler.handleValidation(ex, new MockHttpServletRequest());
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) out.getBody().get("validationErrors");

        assertEquals(400, out.getStatusCode().value());
        assertEquals("Стойността трябва да е точно 10 символа", errors.get("egn"));
    }

    static class Dummy {
        public void setValue(String value) {
        }
    }
}
