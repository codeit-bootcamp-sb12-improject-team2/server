package com.codeit.server.ai.service;

import com.codeit.server.ai.client.GeminiClient;
import com.codeit.server.ai.client.NewsCrawler;
import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

  private final NewsCrawler newsCrawler;
  private final GeminiClient geminiClient;
  @Override
  public NewsSummaryResponseDto summarizeNews(String url, String naverUrl) {

    String articleContent = newsCrawler.crawl(url, naverUrl);

    return geminiClient.summarizeNews(articleContent);
  }
}

