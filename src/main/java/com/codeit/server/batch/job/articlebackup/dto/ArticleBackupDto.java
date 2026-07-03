package com.codeit.server.batch.job.articlebackup.dto;

import com.codeit.server.article.entity.Article;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleBackupDto {

    private UUID id;
    private String source;
    private String sourceUrl;
    private String title;
    private Instant publishDate;
    private String summary;
    private int viewCount;
    private int commentCount;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;

    public static ArticleBackupDto from(Article article) {
        return ArticleBackupDto.builder()
                .id(article.getId())
                .source(article.getSource())
                .sourceUrl(article.getSourceUrl())
                .title(article.getTitle())
                .publishDate(article.getPublishDate())
                .summary(article.getSummary())
                .viewCount(article.getViewCount())
                .commentCount(article.getCommentCount())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .deleted(article.isDeleted())
                .build();
    }

    public Article toEntity() {
        return Article.builder()
                .source(source)
                .sourceUrl(sourceUrl)
                .title(title)
                .publishDate(publishDate)
                .summary(summary)
                .viewCount(viewCount)
                .commentCount(commentCount)
                .isDeleted(deleted)
                .build();
    }
}