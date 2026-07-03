package com.codeit.server.article.service;

import com.codeit.server.article.dto.*;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleView;
import com.codeit.server.article.repository.*;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService{
    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.article-backup-prefix}")
    private String prefix;


    @Transactional
    @Override
    public ArticleViewDto createArticleView(UUID articleId, UUID requestUserId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요

        ArticleView articleView = articleViewRepository
                .findByArticleIdAndUserId(articleId, requestUserId)
                .orElseGet(() -> {
                    ArticleView newView = ArticleView.builder()
                            .articleId(articleId)
                            .userId(requestUserId)
                            .build();

                    article.increaseViewCount(); // dirty checking 트랜잭션 때문에!!!

                    return articleViewRepository.save(newView);
                });

        return ArticleViewDto.from(article, articleView);

    }

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseArticle findArticles(String cursor, String after, int size, UUID requestUserId, ArticleSearchRequest request) {
        return articleRepository.searchArticles(cursor, after, size, requestUserId, request);
    }

    @Transactional(readOnly = true)
    @Override
    public ArticleDto findArticle(UUID articleId, UUID requestUserId) {
        return articleRepository.findArticle(articleId, requestUserId).orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요;
    }

    @Transactional
    @Override
    public void deleteArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요
        article.delete();

    }

    @Transactional
    @Override
    public void hardDeleteArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요
        articleRepository.delete(article);
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> findSource() {
        return articleRepository.findSource();
    }

    @Override
    @Transactional
    public ArticleRestoreResultDto restoreArticles(String from, String to) { // Instant로 받는것 고민해보기
        LocalDate fromDate = parseRestoreDate(from);
        LocalDate toDate = parseRestoreDate(to);

        List<UUID> restoredArticleIds = new ArrayList<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            String key = prefix + "/" + date + "/articles.json";

            List<ArticleBackupDto> backupArticles = readBackupArticles(key);

            for (ArticleBackupDto backupArticle : backupArticles) {
                if (articleRepository.existsBySourceUrl(backupArticle.getSourceUrl())) {
                    continue;
                }

                Article restoredArticle = articleRepository.saveAndFlush(backupArticle.toEntity());

                articleRepository.restoreAuditFields(
                        restoredArticle.getId(),
                        backupArticle.getCreatedAt(),
                        backupArticle.getUpdatedAt()
                );

                restoredArticleIds.add(restoredArticle.getId());
            }
        }

        return ArticleRestoreResultDto.builder()
                .restoreDate(Instant.now())
                .restoredArticleIds(restoredArticleIds)
                .restoredArticleCount(restoredArticleIds.size())
                .build();
    }

    private List<ArticleBackupDto> readBackupArticles(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            String json = s3Client.getObjectAsBytes(request)
                    .asUtf8String();

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<ArticleBackupDto>>() {}
            );

        } catch (NoSuchKeyException e) {
            return List.of();
        } catch (Exception e) {
            throw new IllegalStateException("기사 백업 파일 복구 실패. key=" + key, e);
        }
    }

    private LocalDate parseRestoreDate(String value) {
        try {
            return Instant.parse(value)
                    .atZone(KST)
                    .toLocalDate();
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(value)
                    .atZone(KST)
                    .toLocalDate();
        }
    }
}
