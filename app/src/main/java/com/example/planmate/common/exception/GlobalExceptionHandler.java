package com.example.planmate.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, String> createErrorBody(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return body;
    }

    private String getErrorMessage(Exception ex, String defaultMessage) {
        return (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : defaultMessage;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(createErrorBody(getErrorMessage(ex, "토큰이 인증되지 않았습니다")));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(createErrorBody(getErrorMessage(ex, "사용자를 찾을 수 없습니다")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorBody(getErrorMessage(ex, "잘못된 값이 들어왔습니다")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(createErrorBody(getErrorMessage(ex, "접근 권한이 없습니다")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorBody(getErrorMessage(ex, "서버 오류가 발생했습니다.")));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, String>> handleResourceConflict(ResourceConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(createErrorBody(getErrorMessage(ex, "자원 충돌이 발생했습니다")));
    }
}
