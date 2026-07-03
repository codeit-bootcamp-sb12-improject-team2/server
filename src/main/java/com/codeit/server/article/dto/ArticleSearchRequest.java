package com.codeit.server.article.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
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
