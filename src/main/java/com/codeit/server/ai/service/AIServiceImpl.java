package com.codeit.server.ai.service;

import com.codeit.server.ai.client.GeminiClient;
import com.codeit.server.ai.client.NewsCrawler;
import com.codeit.server.ai.dto.NewsSummaryResponseDto;
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

  @Override
  public NewsSummaryResponseDto summarizeNews(String contentEncoded, String url, String naverUrl) {
    StopWatch stopWatch = new StopWatch("News Summary Performance Tracker");

    String articleContent = "";

    // 1단계: contentEncoded 데이터가 있는 경우 파싱
    stopWatch.start("1. contentEncoded Parsing");
    if (contentEncoded != null && !contentEncoded.isBlank()) {
      articleContent = Jsoup.parse(contentEncoded).text();
    }
    stopWatch.stop();

    // 2단계: 네이버 뉴스 URL이 존재할 경우 우선적으로 크롤링
    if (articleContent.isBlank() && naverUrl != null && !naverUrl.isBlank()) {
      stopWatch.start("2. Naver News Crawling");
      try {
        articleContent = newsCrawler.crawl(naverUrl);
      } catch (Exception e) {
        log.warn("네이버 뉴스 크롤링 실패, 원문 URL로 재시도합니다. naverUrl={}, url={}", naverUrl, url);
      }
      if (stopWatch.isRunning()) {
        stopWatch.stop();
      }
    }

    // 3단계: 일반 언론사 원문 URL만 존재하는 경우 (RSS 등) 크롤링
    if (articleContent.isBlank() && url != null && !url.isBlank()) {
      stopWatch.start("3. Original News Crawling");
      try {
        articleContent = newsCrawler.crawl(url);
      } catch (Exception e) {
        log.error("원문 뉴스 기사 크롤링 실패 url={}", url, e);
      }
      if (stopWatch.isRunning()) {
        stopWatch.stop();
      }
    }

    // 예외 처리: 본문을 어떠한 방법으로도 확보할 수 없는 경우
    if (articleContent.isBlank()) {
      log.warn("기사 본문 확보 실패. 소요시간 로그:\n{}", stopWatch.prettyPrint());
      throw new IllegalArgumentException("요약할 뉴스 본문 콘텐츠를 확보할 수 없습니다.");
    }

    // 4단계: Gemini AI 요약 수행
    stopWatch.start("4. Gemini AI Summarization");
    NewsSummaryResponseDto responseDto = geminiClient.summarizeNews(articleContent);
    stopWatch.stop();

    // 최종 소요시간 분석표를 로그로 기록
    log.info("뉴스 요약 처리 성능 분석 결과:\n{}", stopWatch.prettyPrint());

    return responseDto;
  }
}
