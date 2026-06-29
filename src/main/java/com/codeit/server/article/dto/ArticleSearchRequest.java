package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSearchRequest {
    private String keyword;
    private UUID interestId;
    private List<String> sourceIn;
    private Instant publishDateFrom;
    private Instant publishDateTo;
    private String orderBy;
    private String direction;
}
