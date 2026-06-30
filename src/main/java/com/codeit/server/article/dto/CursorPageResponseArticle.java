package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CursorPageResponseArticle {
    private List<ArticleDto> content;
    private String nextCursor;
    private String nextAfter;
    private int size;
    private long totalElements;
    private boolean hasNext;
}
