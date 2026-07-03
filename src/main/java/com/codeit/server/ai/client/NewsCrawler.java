package com.codeit.server.ai.client;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NewsCrawler {

  public String crawl(String url) {
    try {
      Document document = Jsoup.connect(url)
          .userAgent(
              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/137.0.0.0 Safari/537.36")
          .header("Accept-Language", "ko-KR,ko;q=0.9")
          .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9") // HTML 텍스트 중심 수신
          .maxBodySize(300 * 1024)
          .followRedirects(true)
          .timeout(3000)
          .get();

      return extractContent(document, url);
    } catch (IOException e) {
      log.error("크롤링 실패 url={}", url, e);
      throw new RuntimeException("기사 크롤링 실패", e);
    }
  }

  private String extractContent(Document document, String url) {

    if (url.contains("chosun.com")) {
      try {
        org.jsoup.nodes.Element scriptEl = document.selectFirst("script#fusion-metadata");
        if (scriptEl != null) {
          String scriptData = scriptEl.data();
          int startIdx = scriptData.indexOf("Fusion.globalContent=");
          if (startIdx != -1) {
            startIdx += "Fusion.globalContent=".length();
            String jsonStr = scriptData.substring(startIdx).trim();


            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"content\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(jsonStr);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
              String matched = matcher.group(1);
              matched = translateUnicodeEscapes(matched);
              sb.append(matched).append(" ");
            }

            if (sb.length() > 0) {
              return sb.toString().trim()
                  .replaceAll("<[^>]*>", "")
                  .replace("\\n", "\n")
                  .replace("\\\"", "\"");
            }
          }
        }
      } catch (Exception e) {
        log.warn("조선일보 스크립트 기반 본문 추출 실패, 일반 파싱으로 fallback", e);
      }
    }

    String[] selectors = {
        "#dic_area",
        ".article-view-content-div",
        ".article-body",
        "#article-body",
        "#articleBody",
        ".article_body",
        ".article_content",
        "article",
        "#articleBodyContents"
    };

    for (String selector : selectors) {
      if (!document.select(selector).isEmpty()) {
        return document.select(selector).first().text().trim();
      }
    }


    if (!document.select("p").isEmpty()) {
      return document.select("p").text().trim();
    }

    throw new RuntimeException("기사 본문 추출 실패");
  }

  // Unicode escape 문자 디코더 (역슬래시+u로 시작하는 16진수 시퀀스 -> 한국어 문자)
  private String translateUnicodeEscapes(String s) {
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\\\u([0-9a-fA-F]{4})");
    java.util.regex.Matcher matcher = pattern.matcher(s);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      try {
        char ch = (char) Integer.parseInt(matcher.group(1), 16);
        matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(String.valueOf(ch)));
      } catch (NumberFormatException e) {
        // 무시
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}