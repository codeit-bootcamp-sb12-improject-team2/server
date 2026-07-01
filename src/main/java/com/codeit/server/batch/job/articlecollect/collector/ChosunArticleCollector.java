package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.article.entity.Article;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ChosunArticleCollector extends RssArticleCollector{

    public ChosunArticleCollector(RestClient articleCollectRestClient) {
        super(articleCollectRestClient);
    }

    @Override
    public List<CollectedArticle> collect(String keyword) {
        return collectFromRss(
                "CHOSUN",
                "여기에_조선일보_RSS_URL"
        );
    }
}
