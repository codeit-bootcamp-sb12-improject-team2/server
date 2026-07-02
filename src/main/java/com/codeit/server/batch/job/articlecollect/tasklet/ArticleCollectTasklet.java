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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info(">>>>>> Starting ArticleCollectTasklet");

        List<InterestKeyword> interestKeywords = interestKeywordRepository.findAll();

        log.info("Loaded {} interest keywords.", interestKeywords.size());

        collectFromKeywordSearchCollectors(interestKeywords); // NAVER
        collectFromRssCollectors(interestKeywords);           // RSS

        log.info("Successfully finished ArticleCollectTasklet");

        return RepeatStatus.FINISHED;
    }

    private void collectFromKeywordSearchCollectors(List<InterestKeyword> interestKeywords) {
        Map<String, List<UUID>> keywordInterestIdsMap = interestKeywords.stream()
                .filter(interestKeyword -> interestKeyword.getKeyword() != null)
                .filter(interestKeyword -> !interestKeyword.getKeyword().isBlank())
                .collect(Collectors.groupingBy(
                        interestKeyword -> interestKeyword.getKeyword().trim(),
                        Collectors.mapping(
                                interestKeyword -> interestKeyword.getInterest().getId(),
                                Collectors.toList()
                        )
                ));

        log.info("Start collecting articles from keyword search. keywordCount={}",
                keywordInterestIdsMap.size());

        for (Map.Entry<String, List<UUID>> entry : keywordInterestIdsMap.entrySet()) {
            String keyword = entry.getKey();
            log.debug("Collecting keyword={}", keyword);

            List<UUID> interestIds = entry.getValue();

            for (ArticleCollector collector : articleCollectors) {
                if (!collector.supportsKeywordSearch()) {
                    continue;
                }

                List<CollectedArticle> collectedArticles = collector.collect(keyword);

                for (CollectedArticle collectedArticle : collectedArticles) {
                    Article article = saveArticleIfNotExists(collectedArticle);

                    for (UUID interestId : interestIds) {
                        saveArticleInterestIfNotExists(article.getId(), interestId);
                    }
                }
            }
        }
    }

    private void collectFromRssCollectors(List<InterestKeyword> interestKeywords) {
        for (ArticleCollector collector : articleCollectors) {
            if (collector.supportsKeywordSearch()) {
                continue;
            }

            List<CollectedArticle> collectedArticles = collector.collect();

            for (CollectedArticle collectedArticle : collectedArticles) {
                for (InterestKeyword interestKeyword : interestKeywords) {
                    String keyword = interestKeyword.getKeyword();

                    if (keyword == null || keyword.isBlank()) {
                        continue;
                    }

                    if (!containsKeyword(collectedArticle, keyword)) {
                        continue;
                    }

                    UUID interestId = interestKeyword.getInterest().getId();

                    Article article = saveArticleIfNotExists(collectedArticle);
                    saveArticleInterestIfNotExists(article.getId(), interestId);
                }
            }
        }
    }

    private Article saveArticleIfNotExists(CollectedArticle collectedArticle) {
        return articleRepository.findBySourceUrl(collectedArticle.getSourceUrl())
                .orElseGet(() ->{
                    log.debug("New article saved. source={}, title={}",
                            collectedArticle.getSource(),
                            collectedArticle.getTitle());

                    return articleRepository.save(collectedArticle.toEntity());
                });
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