package com.codeit.server.article.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ArticleRestoreResultDto {
    private Instant restoreDate;
    private List<UUID> restoredArticleIds;
    private int restoredArticleCount;
}
