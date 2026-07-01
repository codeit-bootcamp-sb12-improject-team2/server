package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

@RequiredArgsConstructor
public abstract class RssArticleCollector implements ArticleCollector {

    protected final RestClient articleCollectRestClient;

    @Override
    public boolean supportsKeywordSearch() {
        return false;
    }

    protected List<CollectedArticle> collectFromRss(String source, String rssUrl) {
        String xml = articleCollectRestClient.get()
                .uri(rssUrl)
                .retrieve()
                .body(String.class);

        if (xml == null || xml.isBlank()) {
            return List.of();
        }

        return parseRss(source, xml);
    }

    private List<CollectedArticle> parseRss(String source, String xml) {
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            NodeList items = document.getElementsByTagName("item");

            List<CollectedArticle> articles = new ArrayList<>();

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);

                String title = getText(item, "title");
                String link = getText(item, "link");
                String description = getText(item, "description");
                String pubDate = getText(item, "pubDate");

                if (link == null || link.isBlank()) {
                    continue;
                }

                articles.add(
                        CollectedArticle.builder()
                                .source(source)
                                .sourceUrl(link)
                                .title(cleanText(title))
                                .summary(cleanText(description))
                                .publishDate(parsePubDate(pubDate))
                                .build()
                );
            }

            return articles;
        } catch (Exception e) {
            throw new IllegalStateException("RSS 파싱 실패: " + source, e);
        }
    }

    private String getText(Element item, String tagName) {
        NodeList nodes = item.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            return "";
        }

        Node node = nodes.item(0);

        if (node == null) {
            return "";
        }

        return node.getTextContent();
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