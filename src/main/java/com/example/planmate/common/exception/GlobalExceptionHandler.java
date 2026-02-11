package com.example.planmate.common.exception;

import com.example.planmate.common.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private CommonResponse createErrorBody(String message) {
        CommonResponse response = new CommonResponse();
        response.setMessage(message);
        return response;
    }

    private String getErrorMessage(Exception ex, String defaultMessage) {
        return (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : defaultMessage;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonResponse> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorBody(getErrorMessage(ex, "토큰이 인증되지 않았습니다")));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CommonResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorBody(getErrorMessage(ex, "사용자를 찾을 수 없습니다")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorBody(getErrorMessage(ex, "잘못된 값이 들어왔습니다")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorBody(getErrorMessage(ex, "접근 권한이 없습니다")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorBody(getErrorMessage(ex, "서버 오류가 발생했습니다.")));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<CommonResponse> handleResourceConflict(ResourceConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorBody(getErrorMessage(ex, "자원 충돌이 발생했습니다")));
    }
}
