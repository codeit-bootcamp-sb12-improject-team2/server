package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleRestoreResultDto {
    private Instant restoreDate;
    private List<UUID> restoredArticleIds;
    private int restoredArticleCount;
}
