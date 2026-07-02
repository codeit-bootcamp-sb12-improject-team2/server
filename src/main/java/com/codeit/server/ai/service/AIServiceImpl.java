package com.codeit.server.ai.service;

import com.codeit.server.ai.client.GeminiClient;
import com.codeit.server.ai.client.NewsCrawler;
import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.repository.ArticleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

  private final NewsCrawler newsCrawler;
  private final GeminiClient geminiClient;
  private final ArticleRepository articleRepository;


  @Override
  public NewsSummaryResponseDto summarizeNews(UUID articleId) {

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new RuntimeException("기사 없음"));

    String articleContent = newsCrawler.crawl(article.getSourceUrl());

    return geminiClient.summarizeNews(articleContent);
  }
}
