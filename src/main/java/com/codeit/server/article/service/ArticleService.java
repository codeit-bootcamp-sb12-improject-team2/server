package com.codeit.server.article.service;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.ArticleViewDto;
import com.codeit.server.article.dto.CursorPageResponseArticle;

import java.util.List;
import java.util.UUID;

public interface ArticleService {
    ArticleViewDto createArticleView(UUID articleId, UUID requestUserId);
    CursorPageResponseArticle findArticles(String cursor, String after, int size, UUID requestUserId, ArticleSearchRequest request);
    ArticleDto findArticle(UUID articleId, UUID requestUserId);
    void deleteArticle(UUID articleId);
    void hardDeleteArticle(UUID articleId);
    List<String> findSource();
    // restoreService(String fromDate, String toDate);
}
