package com.codeit.server.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

import com.codeit.server.article.dto.*;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleView;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.article.repository.ArticleViewRepository;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Nested
    @DisplayName("createArticleView 테스트")
    class CreateArticleViewTest {

        @Test
        @DisplayName("성공 - 처음 조회한 기사면 ArticleView를 생성하고 조회수를 증가시킨다")
        void success_createNewArticleView() {
            // Given
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Article article = createArticle(articleId, 0);
            ArticleView savedView = createArticleView(UUID.randomUUID(), articleId, userId);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));
            when(articleViewRepository.findByArticleIdAndUserId(articleId, userId))
                    .thenReturn(Optional.empty());
            when(articleViewRepository.save(any(ArticleView.class)))
                    .thenReturn(savedView);

            // When
            ArticleViewDto result = articleService.createArticleView(articleId, userId);

            // Then
            assertThat(result.getArticleId()).isEqualTo(articleId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(article.getViewCount()).isEqualTo(1);

            verify(articleViewRepository).save(any(ArticleView.class));
        }

        @Test
        @DisplayName("성공 - 이미 조회한 기사면 ArticleView를 새로 생성하지 않고 조회수도 증가시키지 않는다")
        void success_alreadyViewedArticle() {
            // Given
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Article article = createArticle(articleId, 5);
            ArticleView existingView = createArticleView(UUID.randomUUID(), articleId, userId);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));
            when(articleViewRepository.findByArticleIdAndUserId(articleId, userId))
                    .thenReturn(Optional.of(existingView));

            // When
            ArticleViewDto result = articleService.createArticleView(articleId, userId);

            // Then
            assertThat(result.getArticleId()).isEqualTo(articleId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(article.getViewCount()).isEqualTo(5);

            verify(articleViewRepository, never()).save(any(ArticleView.class));
        }

        @Test
        @DisplayName("실패 - 기사가 존재하지 않으면 ARTICLE_NOT_FOUND 예외가 발생한다")
        void fail_articleNotFound() {
            // Given
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> articleService.createArticleView(articleId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("findArticle 테스트")
    class FindArticleTest {

        @Test
        @DisplayName("성공 - 기사 단건을 조회한다")
        void success_findArticle() {
            // Given
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            ArticleDto dto = ArticleDto.builder()
                    .id(articleId)
                    .title("기사 제목")
                    .viewedByMe(true)
                    .build();

            when(articleRepository.findArticle(articleId, userId))
                    .thenReturn(Optional.of(dto));

            // When
            ArticleDto result = articleService.findArticle(articleId, userId);

            // Then
            assertThat(result.getId()).isEqualTo(articleId);
            assertThat(result.getTitle()).isEqualTo("기사 제목");
            assertThat(result.isViewedByMe()).isTrue();
        }

        @Test
        @DisplayName("실패 - 기사가 존재하지 않으면 ARTICLE_NOT_FOUND 예외가 발생한다")
        void fail_articleNotFound() {
            // Given
            UUID articleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(articleRepository.findArticle(articleId, userId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> articleService.findArticle(articleId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("findArticles 테스트")
    class FindArticlesTest {

        @Test
        @DisplayName("성공 - 기사 목록 조회를 Repository에 위임한다")
        void success_findArticles() {
            // Given
            UUID userId = UUID.randomUUID();
            ArticleSearchRequest request = new ArticleSearchRequest(
                    "스프링",
                    null,
                    List.of("NAVER"),
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            CursorPageResponseArticle response = new CursorPageResponseArticle(
                    List.of(),
                    null,
                    null,
                    10,
                    0,
                    false
            );

            when(articleRepository.searchArticles(null, null, 10, userId, request))
                    .thenReturn(response);

            // When
            CursorPageResponseArticle result =
                    articleService.findArticles(null, null, 10, userId, request);

            // Then
            assertThat(result).isSameAs(response);
            verify(articleRepository).searchArticles(null, null, 10, userId, request);
        }
    }

    @Nested
    @DisplayName("deleteArticle 테스트")
    class DeleteArticleTest {

        @Test
        @DisplayName("성공 - 기사를 논리 삭제한다")
        void success_deleteArticle() {
            // Given
            UUID articleId = UUID.randomUUID();
            Article article = createArticle(articleId, 0);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));

            // When
            articleService.deleteArticle(articleId);

            // Then
            assertThat(article.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("실패 - 기사가 존재하지 않으면 ARTICLE_NOT_FOUND 예외가 발생한다")
        void fail_articleNotFound() {
            // Given
            UUID articleId = UUID.randomUUID();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> articleService.deleteArticle(articleId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 이미 논리 삭제된 기사면 ARTICLE_NOT_FOUND 예외가 발생한다")
        void fail_alreadyDeletedArticle() {
            // Given
            UUID articleId = UUID.randomUUID();
            Article article = createArticle(articleId, 0);
            article.delete();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));

            // When & Then
            assertThatThrownBy(() -> articleService.deleteArticle(articleId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("hardDeleteArticle 테스트")
    class HardDeleteArticleTest {

        @Test
        @DisplayName("성공 - 기사를 물리 삭제한다")
        void success_hardDeleteArticle() {
            // Given
            UUID articleId = UUID.randomUUID();
            Article article = createArticle(articleId, 0);

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.of(article));

            // When
            articleService.hardDeleteArticle(articleId);

            // Then
            verify(articleRepository).delete(article);
        }

        @Test
        @DisplayName("실패 - 기사가 존재하지 않으면 ARTICLE_NOT_FOUND 예외가 발생한다")
        void fail_articleNotFound() {
            // Given
            UUID articleId = UUID.randomUUID();

            when(articleRepository.findById(articleId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> articleService.hardDeleteArticle(articleId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("findSource 테스트")
    class FindSourceTest {

        @Test
        @DisplayName("성공 - 고정된 기사 출처 목록을 조회한다")
        void success_findSource() {
            // When
            List<String> result = articleService.findSource();

            // Then
            assertThat(result).containsExactly(
                    "NAVER",
                    "HANKYUNG",
                    "CHOSUN",
                    "YEONHAP"
            );
        }
    }

    private Article createArticle(UUID id, int viewCount) {
        Article article = Article.builder()
                .source("NAVER")
                .sourceUrl("https://news.test/" + id)
                .title("기사 제목")
                .summary("기사 요약")
                .publishDate(Instant.parse("2026-07-06T00:00:00Z"))
                .viewCount(viewCount)
                .commentCount(0)
                .build();

        ReflectionTestUtils.setField(article, "id", id);
        return article;
    }

    private ArticleView createArticleView(UUID id, UUID articleId, UUID userId) {
        ArticleView articleView = ArticleView.builder()
                .articleId(articleId)
                .userId(userId)
                .build();

        ReflectionTestUtils.setField(articleView, "id", id);
        ReflectionTestUtils.setField(articleView, "createdAt", Instant.parse("2026-07-06T00:00:00Z"));
        return articleView;
    }

    @Nested
    @DisplayName("restoreArticles 테스트")
    class RestoreArticlesTest {

        @Test
        @DisplayName("성공 - 백업 기사 중 DB에 없는 기사만 복구한다")
        void success_restoreOnlyNotExistingArticles() throws Exception {
            // Given
            ReflectionTestUtils.setField(articleService, "bucket", "test-bucket");
            ReflectionTestUtils.setField(articleService, "prefix", "article-backups");

            UUID restoredId = UUID.randomUUID();

            ArticleBackupDto existingBackup = createBackupDto(
                    "https://news.test/existing",
                    "이미 존재하는 기사"
            );

            ArticleBackupDto newBackup = createBackupDto(
                    "https://news.test/new",
                    "복구 대상 기사"
            );

            Article savedArticle = createArticle(restoredId, 0);

            String json = "[{}]";
            ResponseBytes<GetObjectResponse> responseBytes =
                    ResponseBytes.fromByteArray(
                            GetObjectResponse.builder().build(),
                            json.getBytes(StandardCharsets.UTF_8)
                    );

            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                    .thenReturn(responseBytes);

            when(objectMapper.readValue(eq(json), any(TypeReference.class)))
                    .thenReturn(List.of(existingBackup, newBackup));

            when(articleRepository.existsBySourceUrl("https://news.test/existing"))
                    .thenReturn(true);
            when(articleRepository.existsBySourceUrl("https://news.test/new"))
                    .thenReturn(false);

            when(articleRepository.saveAndFlush(any(Article.class)))
                    .thenReturn(savedArticle);

            // When
            ArticleRestoreResultDto result = articleService.restoreArticles(
                    "2026-07-06T00:00:00Z",
                    "2026-07-06T00:00:00Z"
            );

            // Then
            assertThat(result.getRestoredArticleCount()).isEqualTo(1);
            assertThat(result.getRestoredArticleIds()).containsExactly(restoredId);

            verify(articleRepository, never()).saveAndFlush(
                    existingBackup.toEntity()
            );
            verify(articleRepository, times(1)).saveAndFlush(any(Article.class));

            verify(articleRepository).restoreAuditFields(
                    eq(restoredId),
                    eq(newBackup.getCreatedAt()),
                    eq(newBackup.getUpdatedAt())
            );
        }

        @Test
        @DisplayName("성공 - 백업 파일이 없으면 빈 결과를 반환한다")
        void success_noSuchKeyReturnEmptyResult() {
            // Given
            ReflectionTestUtils.setField(articleService, "bucket", "test-bucket");
            ReflectionTestUtils.setField(articleService, "prefix", "article-backups");

            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                    .thenThrow(NoSuchKeyException.builder().message("not found").build());

            // When
            ArticleRestoreResultDto result = articleService.restoreArticles(
                    "2026-07-06T00:00:00Z",
                    "2026-07-06T00:00:00Z"
            );

            // Then
            assertThat(result.getRestoredArticleCount()).isZero();
            assertThat(result.getRestoredArticleIds()).isEmpty();

            verify(articleRepository, never()).saveAndFlush(any());
            verify(articleRepository, never()).restoreAuditFields(any(), any(), any());
        }

        @Test
        @DisplayName("성공 - 날짜 범위에 포함된 모든 날짜의 백업 파일을 조회한다")
        void success_restoreDateRange() throws Exception {
            // Given
            ReflectionTestUtils.setField(articleService, "bucket", "test-bucket");
            ReflectionTestUtils.setField(articleService, "prefix", "article-backups");

            String json = "[]";
            ResponseBytes<GetObjectResponse> responseBytes =
                    ResponseBytes.fromByteArray(
                            GetObjectResponse.builder().build(),
                            json.getBytes(StandardCharsets.UTF_8)
                    );

            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                    .thenReturn(responseBytes);

            when(objectMapper.readValue(eq(json), any(TypeReference.class)))
                    .thenReturn(List.of());

            // When
            ArticleRestoreResultDto result = articleService.restoreArticles(
                    "2026-07-06T00:00:00Z",
                    "2026-07-08T00:00:00Z"
            );

            // Then
            assertThat(result.getRestoredArticleCount()).isZero();

            ArgumentCaptor<GetObjectRequest> captor =
                    ArgumentCaptor.forClass(GetObjectRequest.class);

            verify(s3Client, times(3)).getObjectAsBytes(captor.capture());

            assertThat(captor.getAllValues())
                    .extracting(GetObjectRequest::key)
                    .containsExactly(
                            "article-backups/2026-07-06/articles.json",
                            "article-backups/2026-07-07/articles.json",
                            "article-backups/2026-07-08/articles.json"
                    );
        }

        @Test
        @DisplayName("실패 - S3 조회 또는 JSON 파싱 중 예외가 발생하면 IllegalStateException이 발생한다")
        void fail_readBackupArticlesException() {
            // Given
            ReflectionTestUtils.setField(articleService, "bucket", "test-bucket");
            ReflectionTestUtils.setField(articleService, "prefix", "article-backups");

            when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                    .thenThrow(new RuntimeException("S3 error"));

            // When & Then
            assertThatThrownBy(() -> articleService.restoreArticles(
                    "2026-07-06T00:00:00Z",
                    "2026-07-06T00:00:00Z"
            ))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("기사 백업 파일 복구 실패");
        }
    }

    private ArticleBackupDto createBackupDto(String sourceUrl, String title) {
        return ArticleBackupDto.builder()
                .source("NAVER")
                .sourceUrl(sourceUrl)
                .title(title)
                .publishDate(Instant.parse("2026-07-06T00:00:00Z"))
                .summary("요약")
                .viewCount(0)
                .commentCount(0)
                .createdAt(Instant.parse("2026-07-06T01:00:00Z"))
                .updatedAt(Instant.parse("2026-07-06T02:00:00Z"))
                .deleted(false)
                .build();
    }
}