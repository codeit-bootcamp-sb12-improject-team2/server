package com.codeit.server.useractivity.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ArticleActivityDto {

  private final String id;
  private final String articleId;
  private final String source;
  private final String sourceUrl;
  private final String title;
  private final String publishDate;
  private final String summary;
  private final long commentCount;
  private final long viewCount;
  private final boolean viewedByMe;

  public ArticleActivityDto(String id, String articleId, String source, String sourceUrl, String title,
      Instant publishDate, String summary, long commentCount, long viewCount, boolean viewedByMe) {
    this.id = id;
    this.articleId = articleId;
    this.source = source;
    this.sourceUrl = sourceUrl;
    this.title = title;
    // 💡 여기서 핵심! Instant 객체를 자바스크립트가 무조건 좋아하는 표준 문자열로 100% 안전하게 치환합니다.
    this.publishDate = publishDate != null ? publishDate.toString() : null;
    this.summary = summary;
    this.commentCount = commentCount;
    this.viewCount = viewCount;
    this.viewedByMe = viewedByMe;
  }

}
