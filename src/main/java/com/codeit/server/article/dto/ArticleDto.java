package com.codeit.server.article.dto;

import com.codeit.server.article.entity.Article;
import jakarta.persistence.Column;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDto {
    private UUID id;
    private String source;
    private String sourceUrl;
    private String title;
    private Instant publishDate;
    private String summary;
    private int viewCount = 0;
    private int commentCount = 0;

    public static ArticleDto from(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .source(article.getSource())
                .sourceUrl(article.getSourceUrl())
                .title(article.getTitle())
                .publishDate(article.getPublishDate())
                .summary(article.getSummary())
                .viewCount(article.getViewCount())
                .commentCount(article.getCommentCount())
                .build();
    }
}
