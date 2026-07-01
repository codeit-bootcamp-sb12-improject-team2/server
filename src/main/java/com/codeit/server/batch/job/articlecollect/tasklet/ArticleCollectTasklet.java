package com.codeit.server.batch.job.articlecollect.tasklet;

import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleInterest;
import com.codeit.server.article.repository.ArticleInterestRepository;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.batch.job.articlecollect.collector.ArticleCollector;
import com.codeit.server.batch.job.articlecollect.dto.CollectedArticle;
import com.codeit.server.interest.entity.InterestKeyword;
import com.codeit.server.interest.repository.InterestKeywordRepository;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleCollectTasklet implements Tasklet {

    private final List<ArticleCollector> articleCollectors;

    private final InterestKeywordRepository interestKeywordRepository;
    private final ArticleRepository articleRepository;
    private final ArticleInterestRepository articleInterestRepository;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<InterestKeyword> interestKeywords = interestKeywordRepository.findAll();

        for (InterestKeyword interestKeyword : interestKeywords) {
            String keyword = interestKeyword.getKeyword();

            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            for (ArticleCollector collector : articleCollectors) {
                List<CollectedArticle> collectedArticles = collector.collect(keyword);

                for (CollectedArticle collectedArticle : collectedArticles) {
                    if (!collector.supportsKeywordSearch()
                            && !containsKeyword(collectedArticle, keyword)) {
                        continue;
                    }
                    Article article = saveArticleIfNotExists(collectedArticle);

                    saveArticleInterestIfNotExists(
                            article.getId(),
                            interestKeyword.getInterest().getId() // TODO : InterestKeyword에 UUID가 아니라 Interest 통으로 들어가 있음???
//                          interestKeyword.getInterestId()
                    );
                }
            }
        }

        return RepeatStatus.FINISHED;
    }

    private Article saveArticleIfNotExists(CollectedArticle collectedArticle) {
        return articleRepository.findBySourceUrl(collectedArticle.getSourceUrl())
                .orElseGet(() ->
                        articleRepository.save(collectedArticle.toEntity()));
    }

    private void saveArticleInterestIfNotExists(UUID articleId, UUID interestId) {
        if (articleInterestRepository.existsByArticleIdAndInterestId(articleId, interestId)) {
            return;
        }

        articleInterestRepository.save(
                ArticleInterest.builder()
                        .articleId(articleId)
                        .interestId(interestId)
                        .build()
        );
    }

    private boolean containsKeyword(CollectedArticle article, String keyword) {
        String title = article.getTitle() == null ? "" : article.getTitle();
        String summary = article.getSummary() == null ? "" : article.getSummary();

        return title.contains(keyword) || summary.contains(keyword);
    }
}