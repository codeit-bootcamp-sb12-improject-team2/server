package com.codeit.server.article.repository;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.CursorPageResponseArticle;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleInterest;
import com.codeit.server.article.entity.ArticleView;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.codeit.server.global.config.QuerydslConfig;
import com.codeit.server.global.config.JpaAuditingConfig;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import({QuerydslConfig.class, ArticleRepositoryImpl.class, JpaAuditingConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleViewRepository articleViewRepository;

    @Autowired
    private ArticleInterestRepository articleInterestRepository;

    @Nested
    @DisplayName("JpaRepository 기본 메서드 테스트")
    class BasicRepositoryTest {

        @Test
        @DisplayName("성공 - sourceUrl로 기사 조회 및 존재 여부를 확인한다")
        void success_findBySourceUrlAndExistsBySourceUrl() {
            // Given
            Article article = createArticle(
                    "NAVER",
                    "https://news.test/basic-1",
                    "테스트 뉴스 제목",
                    "요약 내용",
                    Instant.parse("2026-07-06T00:00:00Z"),
                    0,
                    0
            );

            articleRepository.save(article);

            // When
            Optional<Article> found = articleRepository.findBySourceUrl("https://news.test/basic-1");
            boolean exists = articleRepository.existsBySourceUrl("https://news.test/basic-1");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("테스트 뉴스 제목");
            assertThat(found.get().getSource()).isEqualTo("NAVER");
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 sourceUrl이면 false를 반환한다")
        void success_existsBySourceUrlFalse() {
            // When
            boolean exists = articleRepository.existsBySourceUrl("https://news.test/not-found");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findArticle 테스트")
    class FindArticleTest {

        @Test
        @DisplayName("성공 - 사용자가 조회한 기사면 viewedByMe가 true다")
        void success_viewedByMeTrue() {
            // Given
            UUID userId = UUID.randomUUID();

            Article article = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/viewed",
                    "조회한 기사",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    3,
                    1
            ));

            articleViewRepository.save(ArticleView.builder()
                    .articleId(article.getId())
                    .userId(userId)
                    .build());

            // When
            Optional<ArticleDto> result = articleRepository.findArticle(article.getId(), userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(article.getId());
            assertThat(result.get().getTitle()).isEqualTo("조회한 기사");
            assertThat(result.get().isViewedByMe()).isTrue();
        }

        @Test
        @DisplayName("성공 - 사용자가 조회하지 않은 기사면 viewedByMe가 false다")
        void success_viewedByMeFalse() {
            // Given
            UUID userId = UUID.randomUUID();

            Article article = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/not-viewed",
                    "조회하지 않은 기사",
                    "요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    0,
                    0
            ));

            // When
            Optional<ArticleDto> result = articleRepository.findArticle(article.getId(), userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().isViewedByMe()).isFalse();
        }

        @Test
        @DisplayName("성공 - 논리 삭제된 기사는 조회되지 않는다")
        void success_deletedArticleNotFound() {
            // Given
            UUID userId = UUID.randomUUID();

            Article article = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/deleted",
                    "삭제된 기사",
                    "요약",
                    Instant.parse("2026-07-06T03:00:00Z"),
                    0,
                    0
            ));
            article.delete();

            // When
            Optional<ArticleDto> result = articleRepository.findArticle(article.getId(), userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchArticles 테스트")
    class SearchArticlesTest {

        @Test
        @DisplayName("성공 - keyword로 제목과 요약을 검색한다")
        void success_searchByKeyword() {
            // Given
            UUID userId = UUID.randomUUID();

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/keyword-1",
                    "스프링 배치 기사",
                    "백엔드 뉴스",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "CHOSUN",
                    "https://news.test/keyword-2",
                    "경제 뉴스",
                    "스프링 관련 요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "HANKYUNG",
                    "https://news.test/keyword-3",
                    "정치 뉴스",
                    "다른 요약",
                    Instant.parse("2026-07-06T03:00:00Z"),
                    1,
                    0
            ));

            ArticleSearchRequest request = new ArticleSearchRequest(
                    "스프링",
                    null,
                    null,
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 10, userId, request);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ArticleDto::getTitle)
                    .containsExactly("경제 뉴스", "스프링 배치 기사");
            assertThat(result.isHasNext()).isFalse();
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 관심사 ID로 기사를 필터링한다")
        void success_filterByInterestId() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            Article matched = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/interest-1",
                    "관심사 매칭 기사",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/interest-2",
                    "관심사 미매칭 기사",
                    "요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    1,
                    0
            ));

            articleInterestRepository.save(ArticleInterest.builder()
                    .articleId(matched.getId())
                    .interestId(interestId)
                    .build());

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    interestId,
                    null,
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 10, userId, request);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(matched.getId());
        }

        @Test
        @DisplayName("성공 - size보다 결과가 많으면 hasNext가 true이고 nextCursor가 생성된다")
        void success_hasNextAndNextCursor() {
            // Given
            UUID userId = UUID.randomUUID();

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/page-1",
                    "기사1",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/page-2",
                    "기사2",
                    "요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/page-3",
                    "기사3",
                    "요약",
                    Instant.parse("2026-07-06T03:00:00Z"),
                    1,
                    0
            ));

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 2, userId, request);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.isHasNext()).isTrue();
            assertThat(result.getNextCursor()).isNotNull();
            assertThat(result.getNextAfter()).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent())
                    .extracting(ArticleDto::getTitle)
                    .containsExactly("기사3", "기사2");
        }

        @Test
        @DisplayName("성공 - sourceIn과 publishDate 기간 조건으로 필터링한다")
        void success_filterBySourceAndPublishDateRange() {
            // Given
            UUID userId = UUID.randomUUID();

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/range-1",
                    "범위 밖 기사",
                    "요약",
                    Instant.parse("2026-07-01T00:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/range-2",
                    "범위 안 기사",
                    "요약",
                    Instant.parse("2026-07-06T00:00:00Z"),
                    1,
                    0
            ));

            articleRepository.save(createArticle(
                    "CHOSUN",
                    "https://news.test/range-3",
                    "출처 제외 기사",
                    "요약",
                    Instant.parse("2026-07-06T00:00:00Z"),
                    1,
                    0
            ));

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    null,
                    List.of("NAVER"),
                    Instant.parse("2026-07-05T00:00:00Z"),
                    Instant.parse("2026-07-07T00:00:00Z"),
                    "publishDate",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 10, userId, request);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("범위 안 기사");
        }

        @Test
        @DisplayName("성공 - 논리 삭제된 기사는 목록에서 제외된다")
        void success_searchArticlesExcludeDeletedArticle() {
            // Given
            UUID userId = UUID.randomUUID();

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/deleted-exclude-1",
                    "정상 기사",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    0,
                    0
            ));

            Article deleted = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/deleted-exclude-2",
                    "삭제 기사",
                    "요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    0,
                    0
            ));
            deleted.delete();

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 10, userId, request);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("정상 기사");
        }

        @Test
        @DisplayName("성공 - 목록 조회에서 사용자가 조회한 기사는 viewedByMe가 true다")
        void success_searchArticlesViewedByMeTrue() {
            // Given
            UUID userId = UUID.randomUUID();

            Article article = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/search-viewed-1",
                    "조회한 기사",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    1,
                    0
            ));

            articleViewRepository.save(ArticleView.builder()
                    .articleId(article.getId())
                    .userId(userId)
                    .build());

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 10, userId, request);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).isViewedByMe()).isTrue();
        }

        @Test
        @DisplayName("성공 - viewCount 기준 내림차순으로 정렬한다")
        void success_searchArticlesOrderByViewCountDesc() {
            // Given
            UUID userId = UUID.randomUUID();

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/view-count-1",
                    "조회수 낮은 기사",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    3,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/view-count-2",
                    "조회수 높은 기사",
                    "요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    10,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/view-count-3",
                    "조회수 중간 기사",
                    "요약",
                    Instant.parse("2026-07-06T03:00:00Z"),
                    5,
                    0
            ));

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "viewCount",
                    "desc"
            );

            // When
            CursorPageResponseArticle result =
                    articleRepository.searchArticles(null, null, 10, userId, request);

            // Then
            assertThat(result.getContent())
                    .extracting(ArticleDto::getTitle)
                    .containsExactly(
                            "조회수 높은 기사",
                            "조회수 중간 기사",
                            "조회수 낮은 기사"
                    );
        }

        @Test
        @DisplayName("성공 - 커서로 다음 페이지를 조회한다")
        void success_searchArticlesNextPageByCursor() {
            // Given
            UUID userId = UUID.randomUUID();

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/cursor-1",
                    "기사1",
                    "요약",
                    Instant.parse("2026-07-06T01:00:00Z"),
                    0,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/cursor-2",
                    "기사2",
                    "요약",
                    Instant.parse("2026-07-06T02:00:00Z"),
                    0,
                    0
            ));

            articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/cursor-3",
                    "기사3",
                    "요약",
                    Instant.parse("2026-07-06T03:00:00Z"),
                    0,
                    0
            ));

            ArticleSearchRequest request = new ArticleSearchRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "publishDate",
                    "desc"
            );

            CursorPageResponseArticle firstPage =
                    articleRepository.searchArticles(null, null, 2, userId, request);

            // When
            CursorPageResponseArticle secondPage =
                    articleRepository.searchArticles(
                            firstPage.getNextCursor(),
                            firstPage.getNextAfter(),
                            2,
                            userId,
                            request
                    );

            // Then
            assertThat(firstPage.getContent())
                    .extracting(ArticleDto::getTitle)
                    .containsExactly("기사3", "기사2");

            assertThat(secondPage.getContent())
                    .extracting(ArticleDto::getTitle)
                    .containsExactly("기사1");

            assertThat(secondPage.isHasNext()).isFalse();
        }
    }


    @Nested
    @DisplayName("findBackup 테스트")
    class FindBackupTest {

        @Test
        @DisplayName("성공 - createdAt 기준으로 백업 대상 기사를 조회한다")
        void success_findBackup() {
            // Given
            Article article = articleRepository.save(createArticle(
                    "NAVER",
                    "https://news.test/backup-1",
                    "백업 기사",
                    "요약",
                    Instant.parse("2026-07-06T00:00:00Z"),
                    10,
                    2
            ));

            Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant end = Instant.now().plus(1, ChronoUnit.DAYS);

            // When
            List<ArticleBackupDto> result = articleRepository.findBackup(start, end);

            // Then
            assertThat(result)
                    .extracting(ArticleBackupDto::getSourceUrl)
                    .contains("https://news.test/backup-1");

            ArticleBackupDto backup = result.stream()
                    .filter(dto -> dto.getSourceUrl().equals(article.getSourceUrl()))
                    .findFirst()
                    .orElseThrow();

            assertThat(backup.getTitle()).isEqualTo("백업 기사");
            assertThat(backup.getViewCount()).isEqualTo(10);
            assertThat(backup.getCommentCount()).isEqualTo(2);
        }
    }

    private Article createArticle(
            String source,
            String sourceUrl,
            String title,
            String summary,
            Instant publishDate,
            int viewCount,
            int commentCount
    ) {
        return Article.builder()
                .source(source)
                .sourceUrl(sourceUrl)
                .title(title)
                .summary(summary)
                .publishDate(publishDate)
                .viewCount(viewCount)
                .commentCount(commentCount)
                .build();
    }

    @Test
    void testSaveAndFindBySourceUrl() {
        // Given
        Article article = Article.builder()
                .source("네이버뉴스")
                .sourceUrl("http://naver.com/test-url-123")
                .title("테스트 뉴스 제목")
                .summary("요약 내용")
                .publishDate(Instant.now())
                .build();

        articleRepository.save(article);

        // When
        Optional<Article> found = articleRepository.findBySourceUrl("http://naver.com/test-url-123");
        boolean exists = articleRepository.existsBySourceUrl("http://naver.com/test-url-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 뉴스 제목");
        assertThat(found.get().getSource()).isEqualTo("네이버뉴스");
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsBySourceUrl_whenNotExists_shouldReturnFalse() {
        // When
        boolean exists = articleRepository.existsBySourceUrl("http://non-existing-url.com");

        // Then
        assertThat(exists).isFalse();
    }
}
