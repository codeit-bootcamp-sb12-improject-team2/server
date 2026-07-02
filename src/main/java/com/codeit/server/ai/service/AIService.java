package com.codeit.server.ai.service;

import com.codeit.server.ai.dto.NewsSummaryResponseDto;

public interface AIService {

  NewsSummaryResponseDto summarizeNews( String contentEncoded, String url, String naverUrl);

}

