package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.article.entity.Article;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;

import java.util.List;

public interface ArticleCollector {

    boolean supportsKeywordSearch();

    default List<CollectedArticle> collect(String keyword) {
        return List.of();
    }

    default List<CollectedArticle> collect() {
        return List.of();
    }
}
