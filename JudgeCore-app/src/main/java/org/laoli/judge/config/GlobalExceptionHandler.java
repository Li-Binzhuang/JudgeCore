package org.laoli.judge.config;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.dto.JudgeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * @Description 全局异常处理器
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<JudgeResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch error: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(JudgeResponse.error(
                        "BAD_REQUEST",
                        "Invalid parameter type: " + e.getName(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JudgeResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(JudgeResponse.error(
                        "BAD_REQUEST",
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<JudgeResponse> handleNullPointer(NullPointerException e) {
        log.error("Null pointer error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(JudgeResponse.error(
                        "BAD_REQUEST",
                        "Missing required parameter",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JudgeResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JudgeResponse.error(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        e.getMessage()
                ));
    }
}
