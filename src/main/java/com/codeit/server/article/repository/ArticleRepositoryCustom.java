package com.codeit.server.article.repository;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.CursorPageResponseArticle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    Optional<ArticleDto> findArticle(UUID articleId, UUID requestUserId);
    CursorPageResponseArticle searchArticles(UUID cursor, Instant after, int size, ArticleSearchRequest request);
    List<String> findSource();
}
