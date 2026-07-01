package com.codeit.server.batch.job.articlecollect.collector;

import com.codeit.server.article.entity.Article;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;

import java.util.List;

public interface ArticleCollector {

    boolean supportsKeywordSearch();

    List<CollectedArticle> collect(String keyword);
}
