package com.codeit.server.ai.service;

import com.codeit.server.ai.dto.NewsSummaryResponseDto;

public interface AIService {

  NewsSummaryResponseDto summarizeNews(String url, String naverUrl);

}

