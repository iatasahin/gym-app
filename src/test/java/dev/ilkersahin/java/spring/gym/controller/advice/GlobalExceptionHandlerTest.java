package dev.ilkersahin.java.spring.gym.controller.advice;

import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.exception.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GlobalExceptionHandlerTest {

    @Mock private HttpServletRequest httpServletRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private static final String TEST_URI = "/api/v1/test";

    @BeforeEach
    void setUp() {
        when(httpServletRequest.getRequestURI()).thenReturn(TEST_URI);
    }

    // =========================================================================
    // AUTHENTICATION / AUTHORIZATION TESTS (100s) - 401, 403
    // =========================================================================

    @Test
    @Order(101)
    void handleInvalidCredentials_shouldReturn401Unauthorized() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid username or password");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentials(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).isEqualTo("Invalid username or password");
        assertThat(response.getBody().path()).isEqualTo(TEST_URI);
    }

    @Test
    @Order(102)
    void handleInvalidCredentials_withCustomMessage_shouldReturnCustomMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Account locked after too many attempts");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentials(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).isEqualTo("Account locked after too many attempts");
    }

    @Test
    @Order(103)
    void handleUnauthorizedAccess_shouldReturn403Forbidden() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException(
                "User 'John.Doe' cannot access resources of 'Jane.Smith'"
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedAccess(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
        assertThat(response.getBody().message()).isEqualTo("User 'John.Doe' cannot access resources of 'Jane.Smith'");
        assertThat(response.getBody().path()).isEqualTo(TEST_URI);
    }

    @Test
    @Order(104)
    void handleUnauthorizedAccess_noAuthenticatedUser_shouldReturn403() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("No authenticated user");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedAccess(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("No authenticated user");
    }

    // =========================================================================
    // NOT FOUND TESTS (200s) - 404
    // =========================================================================

    @Test
    @Order(201)
    void handleEntityNotFound_shouldReturn404NotFound() {
        EntityNotFoundException exception = new EntityNotFoundException("Trainee not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFound(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Trainee not found");
        assertThat(response.getBody().path()).isEqualTo(TEST_URI);
    }

    @Test
    @Order(202)
    void handleTraineeDoesNotExist_shouldReturn404NotFound() {
        TraineeDoesNotExistException exception = new TraineeDoesNotExistException(
                "Trainee with username 'John.Doe' does not exist"
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Trainee with username 'John.Doe' does not exist");
    }

    @Test
    @Order(203)
    void handleTrainerDoesNotExist_shouldReturn404NotFound() {
        TrainerDoesNotExistException exception = new TrainerDoesNotExistException(
                "Trainer with username 'Jane.Smith' does not exist"
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Trainer with username 'Jane.Smith' does not exist");
    }

    @Test
    @Order(204)
    void handleNoHandlerFound_shouldReturn404WithEndpointInfo() {
        NoHandlerFoundException exception = new NoHandlerFoundException(
                "GET", "/api/v1/unknown", null
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoHandlerFound(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Endpoint not found: GET /api/v1/unknown");
    }

    @Test
    @Order(205)
    void handleNoHandlerFound_withPostMethod_shouldReturn404() {
        NoHandlerFoundException exception = new NoHandlerFoundException(
                "POST", "/api/v1/nonexistent", null
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoHandlerFound(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).isEqualTo("Endpoint not found: POST /api/v1/nonexistent");
    }

    // =========================================================================
    // CONFLICT TESTS (300s) - 409
    // =========================================================================

    @Test
    @Order(301)
    void handleUserAlreadyActive_shouldReturn409Conflict() {
        UserAlreadyActiveException exception = new UserAlreadyActiveException("John.Doe");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserAlreadyActive(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("User 'John.Doe' is already active");
        assertThat(response.getBody().path()).isEqualTo(TEST_URI);
    }

    @Test
    @Order(302)
    void handleUserAlreadyInactive_shouldReturn409Conflict() {
        UserAlreadyInactiveException exception = new UserAlreadyInactiveException("Jane.Smith");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserAlreadyInactive(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("User 'Jane.Smith' is already inactive");
        assertThat(response.getBody().path()).isEqualTo(TEST_URI);
    }

    @Test
    @Order(303)
    void handleUserAlreadyActive_withDifferentUsername_shouldIncludeUsername() {
        UserAlreadyActiveException exception = new UserAlreadyActiveException("Bob.Brown");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserAlreadyActive(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).contains("Bob.Brown");
    }

    // =========================================================================
    // VALIDATION TESTS (400s) - 400
    // =========================================================================

    @Test
    @Order(401)
    void handleValidationErrors_withSingleFieldError_shouldReturn400() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "firstName", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Failed");
        assertThat(response.getBody().message()).isEqualTo("firstName: must not be blank");
    }

    @Test
    @Order(402)
    void handleValidationErrors_withMultipleFieldErrors_shouldReturnAllErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("request", "firstName", "must not be blank");
        FieldError fieldError2 = new FieldError("request", "lastName", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).contains("firstName: must not be blank");
        assertThat(response.getBody().message()).contains("lastName: must not be blank");
    }

    @Test
    @Order(403)
    void handleConstraintViolation_shouldReturn400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("username");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolation(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Failed");
        assertThat(response.getBody().message()).isEqualTo("username: must not be blank");
    }

    @Test
    @Order(404)
    void handleConstraintViolation_withMultipleViolations_shouldReturnAllViolations() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("username");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must not be blank");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("password");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("size must be between 4 and 20");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolation(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).contains("username: must not be blank");
        assertThat(response.getBody().message()).contains("password: size must be between 4 and 20");
    }

    @Test
    @Order(405)
    void handleMessageNotReadable_shouldReturn400WithMalformedJsonMessage() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("JSON parse error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMessageNotReadable(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Malformed JSON request body");
    }

    @Test
    @Order(406)
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid training type: INVALID");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid training type: INVALID");
    }

    @Test
    @Order(407)
    void handleIllegalArgument_withDifferentMessage_shouldReturnExactMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("Date cannot be in the future");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).isEqualTo("Date cannot be in the future");
    }

    // =========================================================================
    // METHOD NOT ALLOWED TESTS (500s) - 405
    // =========================================================================

    @Test
    @Order(501)
    void handleMethodNotSupported_shouldReturn405() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodNotSupported(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(405);
        assertThat(response.getBody().error()).isEqualTo("Method Not Allowed");
        assertThat(response.getBody().message()).isEqualTo("Method DELETE not supported for this endpoint");
    }

    @Test
    @Order(502)
    void handleMethodNotSupported_withPatchMethod_shouldReturn405() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("PATCH");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodNotSupported(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).isEqualTo("Method PATCH not supported for this endpoint");
    }

    @Test
    @Order(503)
    void handleMethodNotSupported_withPostMethod_shouldReturn405() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodNotSupported(
                exception, httpServletRequest
        );

        assertThat(response.getBody().message()).isEqualTo("Method POST not supported for this endpoint");
    }

    // =========================================================================
    // CATCH-ALL TESTS (600s) - 500
    // =========================================================================

    @Test
    @Order(601)
    void handleGenericException_shouldReturn500InternalServerError() {
        Exception exception = new Exception("Something went wrong");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().path()).isEqualTo(TEST_URI);
    }

    @Test
    @Order(602)
    void handleGenericException_shouldNotExposeInternalDetails() {
        Exception exception = new RuntimeException("Database connection failed: jdbc:postgresql://localhost:5432");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
                exception, httpServletRequest
        );

        // Should NOT contain internal details
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("Database");
        assertThat(response.getBody().message()).doesNotContain("jdbc");
    }

    @Test
    @Order(603)
    void handleGenericException_withNullPointerException_shouldReturn500() {
        NullPointerException exception = new NullPointerException("null reference");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
                exception, httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }

    // =========================================================================
    // PATH VERIFICATION TESTS (700s)
    // =========================================================================

    @Test
    @Order(701)
    void allHandlers_shouldIncludeCorrectRequestPath() {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/trainees/John.Doe");

        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentials(
                exception, httpServletRequest
        );

        assertThat(response.getBody().path()).isEqualTo("/api/v1/trainees/John.Doe");
    }

    @Test
    @Order(702)
    void errorResponse_shouldIncludeTimestamp() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentials(
                exception, httpServletRequest
        );

        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
