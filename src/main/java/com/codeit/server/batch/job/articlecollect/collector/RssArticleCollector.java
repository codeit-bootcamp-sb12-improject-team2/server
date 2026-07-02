package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.codeit.server.batch.job.articlecollect.dto.RssItem;
import com.codeit.server.batch.job.articlecollect.dto.RssResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestClient;

public abstract class RssArticleCollector implements ArticleCollector {

    protected final RestClient articleCollectRestClient;

    private final XmlMapper xmlMapper = XmlMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    protected RssArticleCollector(RestClient articleCollectRestClient) {
        this.articleCollectRestClient = articleCollectRestClient;
    }

    @Override
    public boolean supportsKeywordSearch() {
        return false;
    }

    protected List<CollectedArticle> collectFromRss(String source, String rssUrl) {
        System.out.println("RSS 호출 source = " + source + ", url = " + rssUrl);

        try {
            String xml = articleCollectRestClient.get()
                    .uri(rssUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36")
                    .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .retrieve()
                    .body(String.class);

            if (xml == null || xml.isBlank()) {
                return List.of();
            }

            RssResponse response = xmlMapper.readValue(xml, RssResponse.class);

            if (response.getChannel() == null || response.getChannel().getItems() == null) {
                return List.of();
            }

            return response.getChannel().getItems().stream()
                    .map(item -> toCollectedArticle(source, item))
                    .filter(article -> article.getSourceUrl() != null && !article.getSourceUrl().isBlank())
                    .toList();

        } catch (Exception e) {
            System.out.println("RSS 수집 실패: " + source + " / " + rssUrl);
            System.out.println(e.getMessage());
            return List.of();
//            throw new IllegalStateException("RSS 파싱 실패: " + source, e);
        }
    }

    private CollectedArticle toCollectedArticle(String source, RssItem item) {
        return CollectedArticle.builder()
                .source(source)
                .sourceUrl(cleanText(item.getLink()))
                .title(cleanText(item.getTitle()))
                .summary(resolveSummary(item))
                .publishDate(parsePubDate(item.getPubDate()))
                .build();
    }

    private String resolveSummary(RssItem item) {
        String description = cleanText(item.getDescription());

        if (description != null && !description.isBlank()) {
            return description;
        }

        return cleanText(item.getContent());
    }

    private Instant parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) {
            return Instant.now();
        }

        try {
            return ZonedDateTime.parse(
                    pubDate,
                    DateTimeFormatter.RFC_1123_DATE_TIME
            ).toInstant();
        } catch (Exception e) {
            return Instant.now();
        }
    }

    private String cleanText(String value) {
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
}