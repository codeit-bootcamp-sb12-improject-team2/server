package com.codeit.server.global.exception;

import com.codeit.server.global.common.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.error("IllegalArgumentException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.BAD_REQUEST.value(),
        "IllegalArgumentException",
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
    log.error("AccessDeniedException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.FORBIDDEN.value(),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
    log.error("NoSuchElementException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
    log.error("EntityNotFoundException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.NOT_FOUND.value(),
        "EntityNotFoundException",
        e.getMessage()
    );
    // 🛠️ 버그 수정: .status() 내부에는 int가 아닌 HttpStatus enum을 넣는 것이 안전합니다.
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
    log.error("IllegalStateException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.BAD_REQUEST.value(),
        "IllegalStateException",
        e.getMessage()
    );
    // 🛠️ 버그 수정: .status() 내부에 int(value) 대신 HttpStatus enum을 넣도록 통일했습니다.
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Exception: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        e.getMessage()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(org.springframework.web.bind.MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException: {}", e.getMessage(), e);

    // 에러 메시지 중 첫 번째 검증 실패 메시지만 깔끔하게 추출
    String bindingMessage = e.getBindingResult().getFieldError() != null
        ? e.getBindingResult().getFieldError().getDefaultMessage()
        : "올바르지 않은 입력값입니다.";

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.BAD_REQUEST.value(),
        "MethodArgumentNotValidException",
        bindingMessage
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
    log.error("MethodArgumentTypeMismatchException: {}", e.getMessage(), e);
    String message = String.format("파라미터 타입이 잘못되었습니다. 필드명: '%s', 기대하는 타입: '%s'",
        e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown");

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.BAD_REQUEST.value(),
        "MethodArgumentTypeMismatchException",
        message
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException e) {
    log.error("HttpMessageNotReadableException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.BAD_REQUEST.value(),
        "HttpMessageNotReadableException",
        "HTTP 요청 본문을 읽을 수 없거나 형식이 잘못되었습니다."
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(org.springframework.web.HttpRequestMethodNotSupportedException e) {
    log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.METHOD_NOT_ALLOWED.value(),
        HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
        String.format("지원하지 않는 HTTP 메서드입니다. (요청된 메서드: %s)", e.getMethod())
    );
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
    log.error("DataIntegrityViolationException: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now().toString(),
        HttpStatus.CONFLICT.value(),
        "DataIntegrityViolationException",
        "데이터 제약 조건 위반이 발생했습니다. (중복 데이터 존재 등)"
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }
}
