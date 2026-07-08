package com.codeit.server.batch.job.articlecollect.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleInterest;
import com.codeit.server.article.repository.ArticleInterestRepository;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.batch.job.articlecollect.collector.ArticleCollector;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.InterestKeyword;
import com.codeit.server.interest.repository.InterestKeywordRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleCollectTaskletTest {

    @Mock
    private ArticleCollector keywordCollector;

    @Mock
    private ArticleCollector rssCollector;

    @Mock
    private InterestKeywordRepository interestKeywordRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    private ArticleCollectTasklet createTasklet() {
        return new ArticleCollectTasklet(
                List.of(keywordCollector, rssCollector),
                interestKeywordRepository,
                articleRepository,
                articleInterestRepository
        );
    }

    @Nested
    @DisplayName("execute 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("성공 - execute는 FINISHED를 반환한다")
        void success_executeReturnFinished() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of());

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            // When
            RepeatStatus result = tasklet.execute(null, null);

            // Then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);
            verify(interestKeywordRepository).findAll();
        }
    }

    @Nested
    @DisplayName("키워드 검색 수집 테스트")
    class KeywordSearchCollectTest {

        @Test
        @DisplayName("성공 - 키워드 검색 Collector로 수집한 새 기사를 저장하고 관심사와 연결한다")
        void success_collectKeywordArticleAndSaveInterest() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            InterestKeyword interestKeyword = createInterestKeyword(interestId, "스프링");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/keyword-1",
                    "스프링 배치 기사",
                    "스프링 관련 요약"
            );

            Article savedArticle = createArticle(articleId, collectedArticle.getSourceUrl());

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);
            when(keywordCollector.collect("스프링"))
                    .thenReturn(List.of(collectedArticle));

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            when(articleRepository.findBySourceUrl(collectedArticle.getSourceUrl()))
                    .thenReturn(Optional.empty());
            when(articleRepository.save(any(Article.class)))
                    .thenReturn(savedArticle);

            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId))
                    .thenReturn(false);

            // When
            RepeatStatus result = tasklet.execute(null, null);

            // Then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);

            verify(keywordCollector).collect("스프링");
            verify(articleRepository).save(any(Article.class));

            ArgumentCaptor<ArticleInterest> captor =
                    ArgumentCaptor.forClass(ArticleInterest.class);

            verify(articleInterestRepository).save(captor.capture());

            assertThat(captor.getValue().getArticleId()).isEqualTo(articleId);
            assertThat(captor.getValue().getInterestId()).isEqualTo(interestId);
        }

        @Test
        @DisplayName("성공 - 같은 keyword를 가진 여러 관심사는 하나의 keyword로 수집하고 각각 연결한다")
        void success_groupSameKeywordAndConnectMultipleInterests() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId1 = UUID.randomUUID();
            UUID interestId2 = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            InterestKeyword keyword1 = createInterestKeyword(interestId1, "스프링");
            InterestKeyword keyword2 = createInterestKeyword(interestId2, "스프링");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/group-keyword",
                    "스프링 기사",
                    "요약"
            );

            Article savedArticle = createArticle(articleId, collectedArticle.getSourceUrl());

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(keyword1, keyword2));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);
            when(keywordCollector.collect("스프링"))
                    .thenReturn(List.of(collectedArticle));

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            when(articleRepository.findBySourceUrl(collectedArticle.getSourceUrl()))
                    .thenReturn(Optional.empty());
            when(articleRepository.save(any(Article.class)))
                    .thenReturn(savedArticle);

            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId1))
                    .thenReturn(false);
            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId2))
                    .thenReturn(false);

            // When
            tasklet.execute(null, null);

            // Then
            verify(keywordCollector, times(1)).collect("스프링");
            verify(articleRepository, times(1)).save(any(Article.class));
            verify(articleInterestRepository, times(2)).save(any(ArticleInterest.class));
        }

        @Test
        @DisplayName("성공 - 키워드는 trim 처리되어 수집된다")
        void success_trimKeyword() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId = UUID.randomUUID();

            InterestKeyword interestKeyword = createInterestKeyword(interestId, "  스프링  ");

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);
            when(keywordCollector.collect("스프링"))
                    .thenReturn(List.of());

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            // When
            tasklet.execute(null, null);

            // Then
            verify(keywordCollector).collect("스프링");
        }

        @Test
        @DisplayName("성공 - 이미 존재하는 기사는 새로 저장하지 않고 관심사만 연결한다")
        void success_existingArticleDoesNotSaveAgain() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            InterestKeyword interestKeyword = createInterestKeyword(interestId, "스프링");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/existing",
                    "스프링 기사",
                    "요약"
            );

            Article existingArticle = createArticle(articleId, collectedArticle.getSourceUrl());

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);
            when(keywordCollector.collect("스프링"))
                    .thenReturn(List.of(collectedArticle));

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            when(articleRepository.findBySourceUrl(collectedArticle.getSourceUrl()))
                    .thenReturn(Optional.of(existingArticle));

            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId))
                    .thenReturn(false);

            // When
            tasklet.execute(null, null);

            // Then
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleInterestRepository).save(any(ArticleInterest.class));
        }

        @Test
        @DisplayName("성공 - 이미 존재하는 ArticleInterest는 중복 저장하지 않는다")
        void success_existingArticleInterestDoesNotSaveAgain() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            InterestKeyword interestKeyword = createInterestKeyword(interestId, "스프링");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/duplicate-interest",
                    "스프링 기사",
                    "요약"
            );

            Article existingArticle = createArticle(articleId, collectedArticle.getSourceUrl());

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);
            when(keywordCollector.collect("스프링"))
                    .thenReturn(List.of(collectedArticle));

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            when(articleRepository.findBySourceUrl(collectedArticle.getSourceUrl()))
                    .thenReturn(Optional.of(existingArticle));

            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId))
                    .thenReturn(true);

            // When
            tasklet.execute(null, null);

            // Then
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleInterestRepository, never()).save(any(ArticleInterest.class));
        }
    }

    @Nested
    @DisplayName("RSS 수집 테스트")
    class RssCollectTest {

        @Test
        @DisplayName("성공 - RSS 기사 제목에 키워드가 포함되면 기사와 관심사를 저장한다")
        void success_collectRssArticleMatchedByTitle() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            InterestKeyword interestKeyword = createInterestKeyword(interestId, "AI");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/rss-title",
                    "AI 산업 뉴스",
                    "요약"
            );

            Article savedArticle = createArticle(articleId, collectedArticle.getSourceUrl());

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of(collectedArticle));

            when(articleRepository.findBySourceUrl(collectedArticle.getSourceUrl()))
                    .thenReturn(Optional.empty());
            when(articleRepository.save(any(Article.class)))
                    .thenReturn(savedArticle);

            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId))
                    .thenReturn(false);

            // When
            tasklet.execute(null, null);

            // Then
            verify(rssCollector).collect();
            verify(articleRepository).save(any(Article.class));
            verify(articleInterestRepository).save(any(ArticleInterest.class));
        }

        @Test
        @DisplayName("성공 - RSS 기사 요약에 키워드가 포함되면 기사와 관심사를 저장한다")
        void success_collectRssArticleMatchedBySummary() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            UUID interestId = UUID.randomUUID();
            UUID articleId = UUID.randomUUID();

            InterestKeyword interestKeyword = createInterestKeyword(interestId, "AI");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/rss-summary",
                    "경제 뉴스",
                    "AI 산업 요약"
            );

            Article savedArticle = createArticle(articleId, collectedArticle.getSourceUrl());

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of(collectedArticle));

            when(articleRepository.findBySourceUrl(collectedArticle.getSourceUrl()))
                    .thenReturn(Optional.empty());
            when(articleRepository.save(any(Article.class)))
                    .thenReturn(savedArticle);

            when(articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId))
                    .thenReturn(false);

            // When
            tasklet.execute(null, null);

            // Then
            verify(articleRepository).save(any(Article.class));
            verify(articleInterestRepository).save(any(ArticleInterest.class));
        }

        @Test
        @DisplayName("성공 - RSS 기사 제목과 요약에 키워드가 없으면 저장하지 않는다")
        void success_notMatchedRssArticleDoesNotSave() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            InterestKeyword interestKeyword = createInterestKeyword(UUID.randomUUID(), "스프링");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/rss-not-matched",
                    "경제 뉴스",
                    "정치 요약"
            );

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of(collectedArticle));

            // When
            tasklet.execute(null, null);

            // Then
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleInterestRepository, never()).save(any(ArticleInterest.class));
        }

        @Test
        @DisplayName("성공 - RSS 기사 title과 summary가 null이어도 저장하지 않고 정상 종료된다")
        void success_rssArticleWithNullTitleAndSummaryDoesNotSave() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            InterestKeyword interestKeyword = createInterestKeyword(UUID.randomUUID(), "AI");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/rss-null",
                    null,
                    null
            );

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(interestKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of(collectedArticle));

            // When
            RepeatStatus result = tasklet.execute(null, null);

            // Then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleInterestRepository, never()).save(any(ArticleInterest.class));
        }
    }

    @Nested
    @DisplayName("빈 키워드 처리 테스트")
    class EmptyKeywordTest {

        @Test
        @DisplayName("성공 - null 또는 blank 키워드는 키워드 검색 수집 조건에서 제외한다")
        void success_ignoreNullAndBlankKeywordInKeywordSearch() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            InterestKeyword nullKeyword = createInterestKeyword(UUID.randomUUID(), null);
            InterestKeyword blankKeyword = createInterestKeyword(UUID.randomUUID(), " ");

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(nullKeyword, blankKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of());

            // When
            tasklet.execute(null, null);

            // Then
            verify(keywordCollector, never()).collect(any(String.class));
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleInterestRepository, never()).save(any(ArticleInterest.class));
        }

        @Test
        @DisplayName("성공 - null 또는 blank 키워드는 RSS 매칭 대상에서 제외한다")
        void success_ignoreNullAndBlankKeywordInRssMatching() {
            // Given
            ArticleCollectTasklet tasklet = createTasklet();

            InterestKeyword nullKeyword = createInterestKeyword(UUID.randomUUID(), null);
            InterestKeyword blankKeyword = createInterestKeyword(UUID.randomUUID(), " ");

            CollectedArticle collectedArticle = createCollectedArticle(
                    "https://news.test/rss-ignore-empty-keyword",
                    "AI 기사",
                    "AI 요약"
            );

            when(interestKeywordRepository.findAll())
                    .thenReturn(List.of(nullKeyword, blankKeyword));

            when(keywordCollector.supportsKeywordSearch())
                    .thenReturn(true);

            when(rssCollector.supportsKeywordSearch())
                    .thenReturn(false);
            when(rssCollector.collect())
                    .thenReturn(List.of(collectedArticle));

            // When
            tasklet.execute(null, null);

            // Then
            verify(articleRepository, never()).save(any(Article.class));
            verify(articleInterestRepository, never()).save(any(ArticleInterest.class));
        }
    }

    private InterestKeyword createInterestKeyword(UUID interestId, String keyword) {
        Interest interest = Interest.builder()
                .name("관심사-" + interestId)
                .subscriberCount(0)
                .build();

        ReflectionTestUtils.setField(interest, "id", interestId);

        InterestKeyword interestKeyword = InterestKeyword.builder()
                .interest(interest)
                .keyword(keyword)
                .build();

        ReflectionTestUtils.setField(interestKeyword, "id", UUID.randomUUID());

        return interestKeyword;
    }

    private CollectedArticle createCollectedArticle(
            String sourceUrl,
            String title,
            String summary
    ) {
        return CollectedArticle.builder()
                .source("NAVER")
                .sourceUrl(sourceUrl)
                .title(title)
                .summary(summary)
                .publishDate(Instant.parse("2026-07-06T00:00:00Z"))
                .build();
    }

    private Article createArticle(UUID articleId, String sourceUrl) {
        Article article = Article.builder()
                .source("NAVER")
                .sourceUrl(sourceUrl)
                .title("기사 제목")
                .summary("기사 요약")
                .publishDate(Instant.parse("2026-07-06T00:00:00Z"))
                .viewCount(0)
                .commentCount(0)
                .build();

        ReflectionTestUtils.setField(article, "id", articleId);

        return article;
    }
}