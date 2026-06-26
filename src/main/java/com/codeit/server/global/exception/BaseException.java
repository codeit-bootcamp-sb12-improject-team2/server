package com.codeit.server.global.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
  private final Instant timestamp;
  private final ErrorCode errorCode;
  private final Map<String, Object> data;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.data = new HashMap<>();
  }

  public BaseException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.data = new HashMap<>();
  }

  public void addDetail(String key, Object value) {
    this.data.put(key, value);
  }
}
