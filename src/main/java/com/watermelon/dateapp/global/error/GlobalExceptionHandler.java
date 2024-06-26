package com.watermelon.dateapp.global.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watermelon.dateapp.global.support.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final Environment env;
    private final ObjectMapper objectMapper;

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        return handleException(
                new ApplicationException(
                        ErrorType.METHOD_NOT_ALLOWED, ErrorType.METHOD_NOT_ALLOWED.getMessage(), e));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMethodNotReadableException(
            HttpMessageNotReadableException e) {
        return handleException(
                new ApplicationException(ErrorType.INVALID_REQUEST_PARAMETER, "잘못된 HttpBody 형식입니다.", e));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleRequestValidException(
            MethodArgumentNotValidException e) {
        StringBuilder builder = new StringBuilder();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(
                        fieldError -> {
                            builder
                                    .append(
                                            String.format(
                                                    "[%s](은)는 %s", fieldError.getField(), fieldError.getDefaultMessage()))
                                    .append("\n");
                        });
        String message = builder.toString();

        return handleException(
                new ApplicationException(ErrorType.INVALID_REQUEST_PARAMETER, message, e));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Object>> handleApplicationException(ApplicationException e) {
        return handleException(e);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<Object>> handleThrowable(Throwable throwable) {
        log.error("[Blossom Exception][UnHandledException] (cause= {})", toDebugData(throwable));
        return handleException(
                new ApplicationException(
                        ErrorType.INTERNAL_PROCESSING_ERROR, throwable.getMessage(), throwable));
    }

    private ResponseEntity<ApiResponse<Object>> handleException(ApplicationException e) {
        return new ResponseEntity<>(
                ApiResponse.error(e.getErrorType(), toErrorData(e.getData()), toDebugData(e.getCause())),
                HttpStatus.valueOf(e.getErrorType().getStatus()));
    }

    private String toErrorData(Object errorData) {
        if (errorData == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(errorData);
        } catch (Exception e) {
            return null;
        }
    }

    private String toDebugData(Throwable throwable) {
        if (!isDevProfile() || throwable == null) {
            return null;
        }
        try {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));

            return sw.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean isDevProfile() {
        String[] activeProfiles = env.getActiveProfiles();

        if (activeProfiles.length == 0) {
            return true;
        } else {
            Set<String> devProfiles = new HashSet<>(Arrays.asList("local", "dev"));
            return Arrays.stream(activeProfiles).anyMatch(devProfiles::contains);
        }
    }
}