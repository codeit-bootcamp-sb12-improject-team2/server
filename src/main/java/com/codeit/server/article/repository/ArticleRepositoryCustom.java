package com.codeit.server.article.repository;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.rank.dto.ArticleRankingDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.CursorPageResponseArticle;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    Optional<ArticleDto> findArticle(UUID articleId, UUID requestUserId);
    CursorPageResponseArticle searchArticles(String cursor, String after, int size, UUID requestUserId, ArticleSearchRequest request);
    List<ArticleRankingDto> findTopArticlesByViewCount(Instant start, Instant end);
    List<ArticleRankingDto> findTopArticlesByCommentCount(Instant start, Instant end);
    List<String> findSource();
    List<ArticleBackupDto> findBackup(Instant start, Instant end);
    void restoreAuditFields(UUID articleId, Instant createdAt, Instant updatedAt);
}
