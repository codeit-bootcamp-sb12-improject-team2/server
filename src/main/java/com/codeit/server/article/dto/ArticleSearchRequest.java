package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
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
