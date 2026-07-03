package com.codeit.server.batch.job.articlecollect.dto;

import com.codeit.server.article.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class CollectedArticle {

    private String source;
    private String sourceUrl;
    private String title;
    private String summary;
    private Instant publishDate;

    public Article toEntity() {
        return Article.builder()
                .source(source)
                .sourceUrl(sourceUrl)
                .title(title)
                .summary(summary)
                .publishDate(publishDate)
                .build();
    }
}