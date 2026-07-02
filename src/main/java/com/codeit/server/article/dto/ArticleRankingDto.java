package com.codeit.server.article.dto;

import com.codeit.server.article.entity.Article;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRankingDto {
  private int rank;
  private UUID articleId;
  private String title;
  private String source;
  private int viewCount;
  private int commentCount;
  private long rankingCount;
  private Instant publishDate;

  public static ArticleRankingDto of(int rank, Article article, long rankingCount) {
    return new ArticleRankingDto(
        rank,
        article.getId(),
        article.getTitle(),
        article.getSource(),
        article.getViewCount(),
        article.getCommentCount(),
        rankingCount,
        article.getPublishDate()
    );
  }
}
