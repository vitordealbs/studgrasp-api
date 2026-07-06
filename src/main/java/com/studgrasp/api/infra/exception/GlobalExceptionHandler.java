package com.studgrasp.api.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fields.put(field, message);
        });
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Error", fields)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null)
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "File size exceeds the maximum allowed 2 MB", null)
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                        "Required part '" + ex.getRequestPartName() + "' is missing", null)
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null)
        );
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON format: {}", ex.getMessage());
        String message = "Invalid JSON format in request body";
        if (ex.getMessage() != null && ex.getMessage().contains("Unrecognized character escape")) {
            message = "Invalid JSON: Special characters in string values must not be escaped with backslash";
        }
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, null)
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                               HttpServletRequest request) {
        String ip = getClientIp(request);
        String path = request.getRequestURI();
        log.warn("[SECURITY] event=FAILED_LOGIN ip={} path={}", ip, path);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "E-mail or Password invalid", null)
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex,
                                                                    HttpServletRequest request) {
        String ip = getClientIp(request);
        String path = request.getRequestURI();
        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        log.warn("[SECURITY] event=UNAUTHORIZED_ACCESS ip={} path={} user={}", ip, path, user);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access Denied", null)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Server Intern Error", null)
        );
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            org.springframework.dao.DataIntegrityViolationException ex) {

        String message = "Operation violated the server integrity";

        if (ex.getMessage() != null && ex.getMessage().contains("users_email_key")) {
            message = "E-mail already registered";
        }

        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, null)
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
