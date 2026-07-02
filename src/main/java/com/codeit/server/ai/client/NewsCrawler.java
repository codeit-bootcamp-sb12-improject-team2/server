package com.codeit.server.ai.client;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NewsCrawler {

  public String crawl(String originalUrl, String naverUrl) {
    try {
      return crawlInternal(originalUrl);

    } catch (Exception e) {
      log.warn("원문 크롤링 실패, 네이버 링크 fallback 시도 url={}", originalUrl);

      try {
        return crawlInternal(naverUrl);

      } catch (Exception ex) {
        log.error("네이버 fallback 크롤링 실패 url={}", naverUrl, ex);
        throw new RuntimeException("뉴스 크롤링 실패");
      }
    }
  }

  private String crawlInternal(String url) throws IOException {
    Document document = Jsoup.connect(url)
        .userAgent(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/137.0.0.0 Safari/537.36")
        .header("Accept-Language", "ko-KR,ko;q=0.9")
        .header("Referer", "https://news.naver.com/")
        .followRedirects(true)
        .timeout(10000)
        .get();

    return extractContent(document);
  }

  private String extractContent(Document document) {
    String[] selectors = {
        "#dic_area",                   // 네이버 뉴스
        ".article-view-content-div",   // 신아일보
        ".article_body",
        "#article-view-content-div",
        "article"
    };

    for (String selector : selectors) {
      if (!document.select(selector).isEmpty()) {
        return document.select(selector).first().text().trim();
      }
    }

    throw new RuntimeException("기사 본문 추출 실패");
  }
}