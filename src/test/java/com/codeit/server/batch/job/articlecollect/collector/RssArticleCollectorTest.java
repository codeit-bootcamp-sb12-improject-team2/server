package com.codeit.server.batch.job.articlecollect.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class RssArticleCollectorTest {

    @Mock
    private RestClient articleCollectRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private TestRssArticleCollector createCollector() {
        return new TestRssArticleCollector(articleCollectRestClient);
    }

    @Nested
    @DisplayName("collect 테스트")
    class CollectTest {

        @Test
        @DisplayName("성공 - RSS XML을 CollectedArticle 목록으로 변환한다")
        void success_collectFromRss() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0">
                      <channel>
                        <item>
                          <title>AI 산업 뉴스</title>
                          <link>https://news.test/rss-1</link>
                          <description>AI 관련 요약</description>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).hasSize(1);

            CollectedArticle article = result.get(0);

            assertThat(article.getSource()).isEqualTo("TEST");
            assertThat(article.getSourceUrl()).isEqualTo("https://news.test/rss-1");
            assertThat(article.getTitle()).isEqualTo("AI 산업 뉴스");
            assertThat(article.getSummary()).isEqualTo("AI 관련 요약");
            assertThat(article.getPublishDate())
                    .isEqualTo(Instant.parse("2026-07-06T01:00:00Z"));
        }

        @Test
        @DisplayName("성공 - RSS item 개수만큼 CollectedArticle을 생성한다")
        void success_collectMultipleItems() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0">
                      <channel>
                        <item>
                          <title>첫 번째 기사</title>
                          <link>https://news.test/rss-1</link>
                          <description>첫 번째 요약</description>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                        <item>
                          <title>두 번째 기사</title>
                          <link>https://news.test/rss-2</link>
                          <description>두 번째 요약</description>
                          <pubDate>Mon, 06 Jul 2026 11:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

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
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0">
                      <channel>
                        <item>
                          <title><![CDATA[  <b>AI</b> &amp; 반도체 뉴스  ]]></title>
                          <link>https://news.test/rss-html</link>
                          <description><![CDATA[  <p>&quot;AI&quot; 산업 &lt;성장&gt;</p>  ]]></description>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("AI & 반도체 뉴스");
            assertThat(result.get(0).getSummary()).isEqualTo("\"AI\" 산업 <성장>");
        }

        @Test
        @DisplayName("성공 - description이 있으면 summary로 description을 사용한다")
        void success_useDescriptionAsSummary() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                      <channel>
                        <item>
                          <title>요약 우선순위 기사</title>
                          <link>https://news.test/rss-description</link>
                          <description>description 요약</description>
                          <content:encoded>content 본문</content:encoded>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSummary()).isEqualTo("description 요약");
        }

        @Test
        @DisplayName("성공 - description이 비어 있으면 content를 summary로 사용한다")
        void success_useContentWhenDescriptionIsBlank() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                      <channel>
                        <item>
                          <title>본문 대체 기사</title>
                          <link>https://news.test/rss-content</link>
                          <description></description>
                          <content:encoded><![CDATA[<p>content 본문 요약</p>]]></content:encoded>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSummary()).isEqualTo("content 본문 요약");
        }

        @Test
        @DisplayName("성공 - description과 content가 비어 있으면 title을 summary로 사용한다")
        void success_useTitleWhenDescriptionAndContentAreBlank() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                      <channel>
                        <item>
                          <title>제목 대체 기사</title>
                          <link>https://news.test/rss-title-summary</link>
                          <description></description>
                          <content:encoded></content:encoded>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSummary()).isEqualTo("제목 대체 기사");
        }

        @Test
        @DisplayName("성공 - sourceUrl이 비어 있는 item은 제외한다")
        void success_filterBlankSourceUrl() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0">
                      <channel>
                        <item>
                          <title>링크 없는 기사</title>
                          <link></link>
                          <description>요약</description>
                          <pubDate>Mon, 06 Jul 2026 10:00:00 +0900</pubDate>
                        </item>
                        <item>
                          <title>링크 있는 기사</title>
                          <link>https://news.test/rss-valid</link>
                          <description>요약</description>
                          <pubDate>Mon, 06 Jul 2026 11:00:00 +0900</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSourceUrl()).isEqualTo("https://news.test/rss-valid");
        }

        @Test
        @DisplayName("성공 - XML이 null이면 빈 리스트를 반환한다")
        void success_nullXmlReturnEmptyList() {
            // Given
            TestRssArticleCollector collector = createCollector();

            mockRestClientResponse(null);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - XML이 blank이면 빈 리스트를 반환한다")
        void success_blankXmlReturnEmptyList() {
            // Given
            TestRssArticleCollector collector = createCollector();

            mockRestClientResponse(" ");

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - channel이 없으면 빈 리스트를 반환한다")
        void success_noChannelReturnEmptyList() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0"></rss>
                    """;

            mockRestClientResponse(xml);

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - XML 파싱에 실패하면 빈 리스트를 반환한다")
        void success_invalidXmlReturnEmptyList() {
            // Given
            TestRssArticleCollector collector = createCollector();

            mockRestClientResponse("<rss><channel><item>");

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - pubDate가 비어 있으면 현재 시간을 사용한다")
        void success_blankPubDateUsesNow() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0">
                      <channel>
                        <item>
                          <title>날짜 없는 기사</title>
                          <link>https://news.test/rss-no-date</link>
                          <description>요약</description>
                          <pubDate></pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            Instant before = Instant.now();

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            Instant after = Instant.now();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPublishDate())
                    .isBetween(before.minus(Duration.ofSeconds(1)), after.plus(Duration.ofSeconds(1)));
        }

        @Test
        @DisplayName("성공 - pubDate 형식이 잘못되면 현재 시간을 사용한다")
        void success_invalidPubDateUsesNow() {
            // Given
            TestRssArticleCollector collector = createCollector();

            String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <rss version="2.0">
                      <channel>
                        <item>
                          <title>날짜 오류 기사</title>
                          <link>https://news.test/rss-invalid-date</link>
                          <description>요약</description>
                          <pubDate>invalid-date</pubDate>
                        </item>
                      </channel>
                    </rss>
                    """;

            mockRestClientResponse(xml);

            Instant before = Instant.now();

            // When
            List<CollectedArticle> result = collector.collect();

            // Then
            Instant after = Instant.now();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPublishDate())
                    .isBetween(before.minus(Duration.ofSeconds(1)), after.plus(Duration.ofSeconds(1)));
        }
    }

    @Nested
    @DisplayName("supportsKeywordSearch 테스트")
    class SupportsKeywordSearchTest {

        @Test
        @DisplayName("성공 - RSS Collector는 keyword 검색을 지원하지 않는다")
        void success_supportsKeywordSearchFalse() {
            // Given
            TestRssArticleCollector collector = createCollector();

            // When
            boolean result = collector.supportsKeywordSearch();

            // Then
            assertThat(result).isFalse();
        }
    }

    private void mockRestClientResponse(String xml) {
        when(articleCollectRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://news.test/rss")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(xml);
    }

    private static class TestRssArticleCollector extends RssArticleCollector {

        public TestRssArticleCollector(RestClient articleCollectRestClient) {
            super(articleCollectRestClient);
        }

        @Override
        public List<CollectedArticle> collect() {
            return collectFromRss("TEST", "https://news.test/rss");
        }
    }
}