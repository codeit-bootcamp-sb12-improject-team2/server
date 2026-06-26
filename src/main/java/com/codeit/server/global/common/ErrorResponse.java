package com.codeit.server.global.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private String timestamp;
  private int status;
  private String message;
  private String details;
}
