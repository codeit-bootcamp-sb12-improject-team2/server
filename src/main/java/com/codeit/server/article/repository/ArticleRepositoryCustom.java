package com.codeit.server.article.repository;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.CursorPageResponse;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    Optional<ArticleDto> findById(UUID id);
    CursorPageResponse<ArticleDto> searchSlice(UUID cursor, int size, ArticleSearchRequest request);
}
