package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.article.entity.Article;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HankyungArticleCollector extends RssArticleCollector {

    public HankyungArticleCollector(RestClient articleCollectRestClient) {
        super(articleCollectRestClient);
    }

    @Override
    public List<CollectedArticle> collect() {
        return collectFromRss(
                "HANKYUNG",
                "https://www.hankyung.com/feed/all-news"
        );
    }
}