package com.codeit.server.article.dto;

import com.codeit.server.article.entity.Article;
import lombok.*;


import java.time.Instant;
import java.util.UUID;

@Getter
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
    private int commentCount;
    private int viewCount;
    private boolean viewedByMe;

}
