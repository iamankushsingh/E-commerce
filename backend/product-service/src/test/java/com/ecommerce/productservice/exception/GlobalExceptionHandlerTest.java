package com.ecommerce.productservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/products");
    }

    @Test
    void testHandleValidationErrors_WithSingleFieldError() {
        // Given
        FieldError fieldError = new FieldError("product", "name", "Product name is required");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleValidationErrors(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo(400);
        assertThat(responseBody.get("error")).isEqualTo("Validation Failed");
        assertThat(responseBody.get("message")).isEqualTo("Invalid input data");
        assertThat(responseBody.get("path")).isEqualTo("uri=/api/products");
        assertThat(responseBody.get("timestamp")).isInstanceOf(LocalDateTime.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) responseBody.get("errors");
        assertThat(errors).hasSize(1);
        assertThat(errors.get("name")).isEqualTo("Product name is required");
    }

    @Test
    void testHandleValidationErrors_WithMultipleFieldErrors() {
        // Given
        FieldError nameError = new FieldError("product", "name", "Product name is required");
        FieldError priceError = new FieldError("product", "price", "Price must be greater than 0");
        FieldError categoryError = new FieldError("product", "category", "Category is required");
        
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(nameError, priceError, categoryError));

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleValidationErrors(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) responseBody.get("errors");
        assertThat(errors).hasSize(3);
        assertThat(errors.get("name")).isEqualTo("Product name is required");
        assertThat(errors.get("price")).isEqualTo("Price must be greater than 0");
        assertThat(errors.get("category")).isEqualTo("Category is required");
    }

    @Test
    void testHandleValidationErrors_WithNoErrors() {
        // Given
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleValidationErrors(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) responseBody.get("errors");
        assertThat(errors).isEmpty();
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Given
        String errorMessage = "Product not found with id: 999";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo(400);
        assertThat(responseBody.get("error")).isEqualTo("Bad Request");
        assertThat(responseBody.get("message")).isEqualTo(errorMessage);
        assertThat(responseBody.get("path")).isEqualTo("uri=/api/products");
        assertThat(responseBody.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void testHandleIllegalArgumentException_WithNullMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("message")).isNull();
    }

    @Test
    void testHandleIllegalArgumentException_WithEmptyMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("message")).isEqualTo("");
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo(500);
        assertThat(responseBody.get("error")).isEqualTo("Internal Server Error");
        assertThat(responseBody.get("message")).isEqualTo("An unexpected error occurred");
        assertThat(responseBody.get("path")).isEqualTo("uri=/api/products");
        assertThat(responseBody.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void testHandleGenericException_WithNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo(500);
        assertThat(responseBody.get("error")).isEqualTo("Internal Server Error");
        assertThat(responseBody.get("message")).isEqualTo("An unexpected error occurred");
    }

    @Test
    void testHandleGenericException_WithArithmeticException() {
        // Given
        ArithmeticException exception = new ArithmeticException("Division by zero");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("message")).isEqualTo("An unexpected error occurred");
    }

    @Test
    void testResponseStructure_ContainsAllRequiredFields() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Test message");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // Verify all required fields are present
        assertThat(responseBody).containsKeys("timestamp", "status", "error", "message", "path");
        
        // Verify field types
        assertThat(responseBody.get("timestamp")).isInstanceOf(LocalDateTime.class);
        assertThat(responseBody.get("status")).isInstanceOf(Integer.class);
        assertThat(responseBody.get("error")).isInstanceOf(String.class);
        assertThat(responseBody.get("message")).isInstanceOf(String.class);
        assertThat(responseBody.get("path")).isInstanceOf(String.class);
    }

    @Test
    void testTimestampAccuracy() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Test message");
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        LocalDateTime timestamp = (LocalDateTime) responseBody.get("timestamp");
        assertThat(timestamp).isBetween(beforeCall, afterCall);
    }

    @Test
    void testWebRequestDescriptionHandling() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/admin/products/1");
        IllegalArgumentException exception = new IllegalArgumentException("Test message");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("path")).isEqualTo("uri=/api/admin/products/1");
        
        // Verify webRequest was called with correct parameter
        verify(webRequest).getDescription(false);
    }

    @Test
    void testDifferentExceptionTypes_ProduceCorrectHttpStatus() {
        // Test IllegalArgumentException -> 400
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Bad argument");
        ResponseEntity<Map<String, Object>> badRequestResponse = 
            globalExceptionHandler.handleIllegalArgumentException(illegalArgException, webRequest);
        assertThat(badRequestResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Test Generic Exception -> 500
        Exception genericException = new Exception("Generic error");
        ResponseEntity<Map<String, Object>> serverErrorResponse = 
            globalExceptionHandler.handleGenericException(genericException, webRequest);
        assertThat(serverErrorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // Test Validation Exception -> 400
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());
        ResponseEntity<Map<String, Object>> validationErrorResponse = 
            globalExceptionHandler.handleValidationErrors(methodArgumentNotValidException, webRequest);
        assertThat(validationErrorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testExceptionHandlerResponseBodyNeverNull() {
        // Test all exception handlers return non-null response bodies
        
        // IllegalArgumentException
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Test");
        ResponseEntity<Map<String, Object>> response1 = 
            globalExceptionHandler.handleIllegalArgumentException(illegalArgException, webRequest);
        assertThat(response1.getBody()).isNotNull();

        // Generic Exception
        Exception genericException = new Exception("Test");
        ResponseEntity<Map<String, Object>> response2 = 
            globalExceptionHandler.handleGenericException(genericException, webRequest);
        assertThat(response2.getBody()).isNotNull();

        // Validation Exception
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());
        ResponseEntity<Map<String, Object>> response3 = 
            globalExceptionHandler.handleValidationErrors(methodArgumentNotValidException, webRequest);
        assertThat(response3.getBody()).isNotNull();
    }
} 