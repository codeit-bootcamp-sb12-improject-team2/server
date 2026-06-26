package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class ArticleViewDto {
    private UUID id;
    private UUID userId;
    private Instant cratedAt;
    private UUID articleId;
    private String source;
    private String sourceUrl;
    private String articleTitle;
    private Instant articlePublishedDate;
    private String articleSummary;
    private int articleCommentCount;
    private int articleViewCount;

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