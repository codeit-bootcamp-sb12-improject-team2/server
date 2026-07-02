package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class YonhapArticleCollector extends RssArticleCollector {

    public YonhapArticleCollector(RestClient articleCollectRestClient) {
        super(articleCollectRestClient);
    }

    @Override
    public List<CollectedArticle> collect() {
        return collectFromRss(
                "YONHAP",
                "https://www.yonhapnewstv.co.kr/browse/feed/"
        );
    }
}