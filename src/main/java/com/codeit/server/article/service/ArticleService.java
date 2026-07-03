package com.codeit.server.article.service;

import com.codeit.server.article.dto.*;

import java.util.List;
import java.util.UUID;

public interface ArticleService {
    ArticleViewDto createArticleView(UUID articleId, UUID requestUserId);
    CursorPageResponseArticle findArticles(String cursor, String after, int size, UUID requestUserId, ArticleSearchRequest request);
    ArticleDto findArticle(UUID articleId, UUID requestUserId);
    void deleteArticle(UUID articleId);
    void hardDeleteArticle(UUID articleId);
    List<String> findSource();
    ArticleRestoreResultDto restoreArticles(String from, String to);
}
