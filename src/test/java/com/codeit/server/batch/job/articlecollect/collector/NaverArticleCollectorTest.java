package com.codeit.server.batch.job.articlecollect.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.codeit.server.batch.job.articlecollect.dto.NaverArticleItem;
import com.codeit.server.batch.job.articlecollect.dto.NaverArticleResponse;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@ExtendWith(MockitoExtension.class)
class NaverArticleCollectorTest {

    @Mock
    private RestClient articleCollectRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private NaverArticleCollector createCollector() {
        NaverArticleCollector collector = new NaverArticleCollector(articleCollectRestClient);

        ReflectionTestUtils.setField(collector, "clientId", "test-client-id");
        ReflectionTestUtils.setField(collector, "clientSecret", "test-client-secret");

        return collector;
    }

    @Nested
    @DisplayName("collect 테스트")
    class CollectTest {

        @Test
        @DisplayName("성공 - 네이버 API 응답을 CollectedArticle 목록으로 변환한다")
        void success_collectNaverArticles() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "<b>AI</b> 산업 뉴스",
                            "https://original.news.test/1",
                            "https://naver.news.test/1",
                            "<p>AI &amp; 반도체 요약</p>",
                            "Mon, 06 Jul 2026 10:00:00 +0900"
                    )
            ));

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("AI");

            // Then
            assertThat(result).hasSize(1);

            CollectedArticle article = result.get(0);

            assertThat(article.getSource()).isEqualTo("NAVER");
            assertThat(article.getSourceUrl()).isEqualTo("https://original.news.test/1");
            assertThat(article.getTitle()).isEqualTo("AI 산업 뉴스");
            assertThat(article.getSummary()).isEqualTo("AI & 반도체 요약");
            assertThat(article.getPublishDate())
                    .isEqualTo(Instant.parse("2026-07-06T01:00:00Z"));
        }

        @Test
        @DisplayName("성공 - 네이버 응답 item 개수만큼 CollectedArticle을 생성한다")
        void success_collectMultipleItems() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "첫 번째 기사",
                            "https://original.news.test/1",
                            "https://naver.news.test/1",
                            "첫 번째 요약",
                            "Mon, 06 Jul 2026 10:00:00 +0900"
                    ),
                    createItem(
                            "두 번째 기사",
                            "https://original.news.test/2",
                            "https://naver.news.test/2",
                            "두 번째 요약",
                            "Mon, 06 Jul 2026 11:00:00 +0900"
                    )
            ));

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("뉴스");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(CollectedArticle::getTitle)
                    .containsExactly("첫 번째 기사", "두 번째 기사");
        }

        @Test
        @DisplayName("성공 - HTML 태그와 엔티티를 제거한다")
        void success_cleanHtmlTagsAndEntities() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "  <b>AI</b> &amp; 반도체 뉴스  ",
                            "https://original.news.test/html",
                            "https://naver.news.test/html",
                            "  <p>&quot;AI&quot; 산업 &lt;성장&gt;</p>  ",
                            "Mon, 06 Jul 2026 10:00:00 +0900"
                    )
            ));

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("AI");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("AI & 반도체 뉴스");
            assertThat(result.get(0).getSummary()).isEqualTo("\"AI\" 산업 <성장>");
        }

        @Test
        @DisplayName("성공 - originallink가 있으면 sourceUrl로 originallink를 사용한다")
        void success_useOriginalLinkAsSourceUrl() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "원본 링크 기사",
                            "https://original.news.test/original",
                            "https://naver.news.test/original",
                            "요약",
                            "Mon, 06 Jul 2026 10:00:00 +0900"
                    )
            ));

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("뉴스");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSourceUrl())
                    .isEqualTo("https://original.news.test/original");
        }

        @Test
        @DisplayName("성공 - originallink가 비어 있으면 link를 sourceUrl로 사용한다")
        void success_useLinkWhenOriginalLinkIsBlank() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "네이버 링크 기사",
                            "",
                            "https://naver.news.test/fallback",
                            "요약",
                            "Mon, 06 Jul 2026 10:00:00 +0900"
                    )
            ));

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("뉴스");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSourceUrl())
                    .isEqualTo("https://naver.news.test/fallback");
        }

        @Test
        @DisplayName("성공 - sourceUrl이 비어 있는 기사는 제외한다")
        void success_filterBlankSourceUrl() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "링크 없는 기사",
                            "",
                            "",
                            "요약",
                            "Mon, 06 Jul 2026 10:00:00 +0900"
                    ),
                    createItem(
                            "링크 있는 기사",
                            "https://original.news.test/valid",
                            "https://naver.news.test/valid",
                            "요약",
                            "Mon, 06 Jul 2026 11:00:00 +0900"
                    )
            ));

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("뉴스");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("링크 있는 기사");
            assertThat(result.get(0).getSourceUrl())
                    .isEqualTo("https://original.news.test/valid");
        }

        @Test
        @DisplayName("성공 - 네이버 API 응답이 null이면 빈 리스트를 반환한다")
        void success_nullResponseReturnEmptyList() {
            // Given
            NaverArticleCollector collector = createCollector();

            mockRestClientResponse(null);

            // When
            List<CollectedArticle> result = collector.collect("뉴스");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 응답 items가 null이면 빈 리스트를 반환한다")
        void success_nullItemsReturnEmptyList() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = new NaverArticleResponse();

            mockRestClientResponse(response);

            // When
            List<CollectedArticle> result = collector.collect("뉴스");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - pubDate 형식이 잘못되면 DateTimeParseException이 발생한다")
        void fail_invalidPubDate() {
            // Given
            NaverArticleCollector collector = createCollector();

            NaverArticleResponse response = createResponse(List.of(
                    createItem(
                            "날짜 오류 기사",
                            "https://original.news.test/invalid-date",
                            "https://naver.news.test/invalid-date",
                            "요약",
                            "invalid-date"
                    )
            ));

            mockRestClientResponse(response);

            // When & Then
            assertThatThrownBy(() -> collector.collect("뉴스"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("supportsKeywordSearch 테스트")
    class SupportsKeywordSearchTest {

        @Test
        @DisplayName("성공 - 네이버 Collector는 keyword 검색을 지원한다")
        void success_supportsKeywordSearchTrue() {
            // Given
            NaverArticleCollector collector = createCollector();

            // When
            boolean result = collector.supportsKeywordSearch();

            // Then
            assertThat(result).isTrue();
        }
    }

    private void mockRestClientResponse(NaverArticleResponse response) {
        when(articleCollectRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NaverArticleResponse.class)).thenReturn(response);
    }

    private NaverArticleResponse createResponse(List<NaverArticleItem> items) {
        NaverArticleResponse response = new NaverArticleResponse();
        ReflectionTestUtils.setField(response, "items", items);
        return response;
    }

    private NaverArticleItem createItem(
            String title,
            String originalLink,
            String link,
            String description,
            String pubDate
    ) {
        NaverArticleItem item = new NaverArticleItem();

        ReflectionTestUtils.setField(item, "title", title);
        ReflectionTestUtils.setField(item, "originallink", originalLink);
        ReflectionTestUtils.setField(item, "link", link);
        ReflectionTestUtils.setField(item, "description", description);
        ReflectionTestUtils.setField(item, "pubDate", pubDate);

        return item;
    }
}