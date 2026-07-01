package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import org.springframework.web.client.RestClient;

import java.util.List;

public class YonhapArticleCollector extends RssArticleCollector{
    public YonhapArticleCollector(RestClient articleCollectRestClient) {
        super(articleCollectRestClient);
    }

    @Override
    public List<CollectedArticle> collect(String keyword) {
        return collectFromRss(
                "YONHAP",
                "여기에_연합뉴스_RSS_URL"
        );
    }
}
