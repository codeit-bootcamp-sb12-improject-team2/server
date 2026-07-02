package com.codeit.server.global.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    log.error("비즈니스 예외 발생 - Code: {}, Message: {}, Data: {}",
        e.getErrorCode().name(), e.getMessage(), e.getData());

    HttpStatus status = parseHttpStatus(e.getErrorCode());

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        e.getErrorCode().name(),
        e.getMessage(),
        e.getData(),
        e.getClass().getSimpleName(),
        status.value()
    );

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException: {}", e.getMessage(), e);

    Map<String, Object> details = new LinkedHashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String field = ((FieldError) error).getField();
      details.put(field, error.getDefaultMessage());
    });

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.VALIDATION_ERROR.name(),
        ErrorCode.VALIDATION_ERROR.getMessage(),
        details,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    log.error("ConstraintViolationException: {}", e.getMessage(), e);

    Map<String, Object> details = new LinkedHashMap<>();
    e.getConstraintViolations().forEach(cv -> {
      String path = cv.getPropertyPath().toString();
      String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
      details.put(field, cv.getMessage());
    });

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.VALIDATION_ERROR.name(),
        ErrorCode.VALIDATION_ERROR.getMessage(),
        details,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    log.error("MethodArgumentTypeMismatchException: {}", e.getMessage(), e);

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("field", e.getName());
    details.put("rejectedValue", e.getValue());

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.TYPE_MISMATCH.name(),
        ErrorCode.TYPE_MISMATCH.getMessage(),
        details,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
    log.error("MissingServletRequestPartException: {}", e.getMessage(), e);

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("missingPartName", e.getRequestPartName());

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.INVALID_REQUEST.name(),
        "필수 요청 파트가 누락되었습니다.",
        details,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception e) {
    log.error("BadRequest Exception: {}", e.getMessage(), e);
    ErrorResponse response = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 🛠️ 임포트 꼬임 방지를 위해 스프링 시큐리티 풀 패키지 경로로 명시
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
    log.error("AccessDeniedException: {}", e.getMessage(), e);
    ErrorResponse response = new ErrorResponse(e, HttpStatus.FORBIDDEN.value());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundExceptions(Exception e) {
    log.error("NotFound Exception: {}", e.getMessage(), e);
    ErrorResponse response = new ErrorResponse(e, HttpStatus.NOT_FOUND.value());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
    log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage(), e);

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("requestedMethod", e.getMethod());

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.METHOD_NOT_ALLOWED.name(),
        ErrorCode.METHOD_NOT_ALLOWED.getMessage(),
        details,
        e.getClass().getSimpleName(),
        HttpStatus.METHOD_NOT_ALLOWED.value()
    );
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    log.error("DataIntegrityViolationException: {}", e.getMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.DATA_CONFLICT.name(),
        ErrorCode.DATA_CONFLICT.getMessage(),
        new HashMap<>(),
        e.getClass().getSimpleName(),
        HttpStatus.CONFLICT.value()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    log.error("MaxUploadSizeExceededException: {}", e.getMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.FILE_SIZE_EXCEEDED.name(),
        ErrorCode.FILE_SIZE_EXCEEDED.getMessage(),
        new HashMap<>(),
        e.getClass().getSimpleName(),
        HttpStatus.PAYLOAD_TOO_LARGE.value()
    );
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<ErrorResponse> handleIOException(IOException e) {
    log.error("IOException: {}", e.getMessage(), e);
    ErrorResponse response = new ErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled Exception: {}", e.getMessage(), e);
    ErrorResponse response = new ErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  private HttpStatus parseHttpStatus(ErrorCode errorCode) {
    return switch (errorCode) {
      case USER_NOT_FOUND, ARTICLE_NOT_FOUND, NOTIFICATION_NOT_FOUND,
           INTEREST_NOT_FOUND, SUBSCRIPTION_NOT_FOUND, COMMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;

      case USER_ALREADY_EXISTS, USER_NICKNAME_ALREADY_EXISTS, INTEREST_ALREADY_EXISTS,
           ALREADY_SUBSCRIBED, ALREADY_LIKED_COMMENT, ARTICLE_ALREADY_DELETED,
           COMMENT_ALREADY_DELETED, DATA_CONFLICT -> HttpStatus.CONFLICT;

      case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;

      case USER_ACCESS_DENIED, NOTIFICATION_ACCESS_DENIED, COMMENT_ACCESS_DENIED -> HttpStatus.FORBIDDEN;

      case INVALID_REQUEST, VALIDATION_ERROR, TYPE_MISMATCH,
           ARTICLE_NOT_DELETED, COMMENT_LIKE_NOT_FOUND,
           COMMENT_CONTENT_BLOCKED, INVALID_RANKING_TYPE -> HttpStatus.BAD_REQUEST;

      case COMMENT_MODERATION_FAILED -> HttpStatus.SERVICE_UNAVAILABLE;

      case FILE_SIZE_EXCEEDED -> HttpStatus.PAYLOAD_TOO_LARGE;

      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}