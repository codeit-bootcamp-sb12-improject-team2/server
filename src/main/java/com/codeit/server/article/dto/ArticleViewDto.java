package com.codeit.server.article.dto;

import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleView;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleViewDto {
    private UUID id;
    private UUID userId;
    private Instant createdAt;
    private UUID articleId;
    private String source;
    private String sourceUrl;
    private String articleTitle;
    private Instant articlePublishedDate;
    private String articleSummary;
    private int articleCommentCount;
    private int articleViewCount;

    public static ArticleViewDto from(Article article, ArticleView articleView) {
        return new ArticleViewDto(
                articleView.getId(),
                articleView.getUserId(),
                articleView.getCreatedAt(),
                article.getId(),
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                article.getCommentCount(),
                article.getViewCount()
        );
    }

}

//id uuid
//viewedBy uuid
//createdAt date-time
//articleId uuid
//source string
//sourceUrl string
//articleTitle string
//articlePublishedDate date-time
//articleSummary string
//articleCommentCount int64
//articleViewCount int64