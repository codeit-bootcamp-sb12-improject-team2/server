package com.codeit.server.ai.dto;

import lombok.Getter;

@Getter
public class NewsSummaryRequestDto {
  private String contentEncoded;
  private String url;
  private String naverUrl;
}
