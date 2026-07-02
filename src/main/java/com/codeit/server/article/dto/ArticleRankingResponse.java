package com.codeit.server.article.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRankingResponse {
  private LocalDate date;
  private String type;
  private List<ArticleRankingDto> articles;

  public static ArticleRankingResponse of(
      LocalDate date,
      String type,
      List<ArticleRankingDto> articles
  ) {
    return new ArticleRankingResponse(date, type, articles);
  }
}
