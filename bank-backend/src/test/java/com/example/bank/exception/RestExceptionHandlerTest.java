package com.example.bank.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("Стойността трябва да е точно 10 символа", out.getBody().get("error"));
    }

    @Test
    void handleValidationSummaryJoinsDistinctMessages() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj", "a", "must not be blank"));
        binding.addError(new FieldError("obj", "b", "must not be blank"));
        Method method = Dummy.class.getMethod("setValue", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<Map<String, Object>> out = handler.handleValidation(ex, new MockHttpServletRequest());
        assertEquals("Полето е задължително", out.getBody().get("error"));
    }

    @Test
    void handleValidationFallsBackWhenNoFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new ObjectError("obj", "custom"));
        Method method = Dummy.class.getMethod("setValue", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<Map<String, Object>> out = handler.handleValidation(ex, new MockHttpServletRequest());
        assertEquals("custom", out.getBody().get("error"));
    }

    @Test
    void handleTypeMismatchReturns400() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "x", Integer.class, "clientId", null, null);
        ResponseEntity<Map<String, Object>> out = handler.handleTypeMismatch(ex, new MockHttpServletRequest());
        assertEquals(400, out.getStatusCode().value());
        assertEquals("Невалиден формат на параметър: clientId", out.getBody().get("error"));
    }

    @Test
    void handleNotReadableReturns400() {
        ResponseEntity<Map<String, Object>> out = handler.handleNotReadable(
                new HttpMessageNotReadableException("bad", null, null),
                new MockHttpServletRequest());
        assertEquals(400, out.getStatusCode().value());
        assertEquals("Невалиден формат на заявката", out.getBody().get("error"));
    }

    @Test
    void handleConstraintViolationReturns400() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);
        Mockito.when(path.toString()).thenReturn("fieldX");
        Mockito.when(v.getPropertyPath()).thenReturn(path);
        Mockito.when(v.getMessage()).thenReturn("msg");
        Set<ConstraintViolation<?>> set = new HashSet<>();
        set.add(v);
        ResponseEntity<Map<String, Object>> out = handler.handleConstraintViolation(
                new ConstraintViolationException(set),
                new MockHttpServletRequest());
        assertEquals(400, out.getStatusCode().value());
        assertEquals("Невалидни параметри", out.getBody().get("error"));
        @SuppressWarnings("unchecked")
        Map<String, String> ve = (Map<String, String>) out.getBody().get("validationErrors");
        assertTrue(ve.containsKey("fieldX"));
    }

    @Test
    void handleIllegalArgumentReturns400() {
        ResponseEntity<Map<String, Object>> out = handler.handleIllegalArgument(
                new IllegalArgumentException("oops"),
                new MockHttpServletRequest());
        assertEquals(400, out.getStatusCode().value());
        assertEquals("oops", out.getBody().get("error"));
    }

    @Test
    void handleUnexpectedReturns500() {
        ResponseEntity<Map<String, Object>> out = handler.handleUnexpected(
                new RuntimeException("boom"),
                new MockHttpServletRequest());
        assertEquals(500, out.getStatusCode().value());
        assertEquals("Вътрешна грешка в сървъра", out.getBody().get("error"));
    }

    @Test
    void buildErrorWithNullRequestHasEmptyPath() {
        ResponseEntity<Map<String, Object>> out = handler.handleBusiness(
                new BusinessException("x"),
                null);
        assertEquals("", out.getBody().get("path"));
    }

    static class Dummy {
        public void setValue(String value) {
        }
    }
}
