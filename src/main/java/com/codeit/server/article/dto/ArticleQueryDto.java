package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ArticleQueryDto {
    private UUID id;
    private String source;
    private String sourceUrl;
    private String title;
    private Instant publishDate;
    private String summary;
    private int commentCount;
    private int viewCount;
    private boolean viewedByMe;
    private Instant createdAt;

    public ArticleDto toArticleDto() {
        return new ArticleDto(
                id,
                source,
                sourceUrl,
                title,
                publishDate,
                summary,
                commentCount,
                viewCount,
                viewedByMe
        );
    }
}
