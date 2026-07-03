package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.article.entity.Article;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.codeit.server.batch.job.articlecollect.dto.NaverArticleItem;
import com.codeit.server.batch.job.articlecollect.dto.NaverArticleResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class NaverArticleCollector implements ArticleCollector {

    private final RestClient articleCollectRestClient;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Override
    public List<CollectedArticle> collect(String keyword) {
        sleep();
        NaverArticleResponse response = articleCollectRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/news.json")
                        .queryParam("query", keyword)
                        .queryParam("display", 10) // 100은 너무 빨라 속도 제한
                        .queryParam("start", 1)
                        .queryParam("sort", "date")
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .body(NaverArticleResponse.class);
        System.out.println(response);


        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(this::toCollectedArticle)
                .filter(article -> article.getSourceUrl() != null && !article.getSourceUrl().isBlank())
                .toList();
    }

    @Override
    public boolean supportsKeywordSearch() {
        return true;
    }

    private CollectedArticle toCollectedArticle(NaverArticleItem item) {
        return CollectedArticle.builder()
                .source("NAVER")
                .sourceUrl(resolveSourceUrl(item))
                .title(cleanHtml(item.getTitle()))
                .summary(cleanHtml(item.getDescription()))
                .publishDate(parsePubDate(item.getPubDate()))
                .build();
    }

    private String resolveSourceUrl(NaverArticleItem item) {
        if (item.getOriginallink() != null && !item.getOriginallink().isBlank()) {
            return item.getOriginallink();
        }
        return item.getLink();
    }

    private Instant parsePubDate(String pubDate) {
        return ZonedDateTime.parse(
                pubDate,
                DateTimeFormatter.RFC_1123_DATE_TIME
        ).toInstant();
    }

    private String cleanHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private void sleep() {
        try {
            Thread.sleep(800); // 0.8초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}