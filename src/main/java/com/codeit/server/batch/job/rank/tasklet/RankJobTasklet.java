package com.codeit.server.batch.job.rank.tasklet;

import com.codeit.server.article.dto.ArticleRankingDto;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleRanking;
import com.codeit.server.article.repository.ArticleRankingRepository;
import com.codeit.server.article.repository.ArticleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
public class RankJobTasklet implements Tasklet {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final ArticleRepository articleRepository;
    private final ArticleRankingRepository articleRankingRepository;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>>>> Starting RankJobTasklet");
        String targetDateStr = (String) chunkContext.getStepContext()
                .getJobParameters()
                .get("targetDate");

        LocalDate targetDate;
        if (targetDateStr != null) {
            targetDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            targetDate = LocalDate.now(KST).minusDays(1);
        }

        Instant start = targetDate.atStartOfDay(KST).toInstant();
        Instant end = targetDate.plusDays(1).atStartOfDay(KST).toInstant();

        log.info("Rank calculations target date={}, start={}, end={}", targetDate, start, end);
        articleRankingRepository.deleteByRankingDate(targetDate);

        List<ArticleRanking> rankingEntities = new ArrayList<>();

        // 2. Fetch and prepare VIEW ranking
        List<ArticleRankingDto> viewRankings = articleRepository.findTopArticlesByViewCount(start, end);
        for (ArticleRankingDto dto : viewRankings) {
            Article articleProxy = articleRepository.getReferenceById(dto.getArticleId());
            rankingEntities.add(ArticleRanking.builder()
                    .rankingDate(targetDate)
                    .rankType("VIEW")
                    .ranking(dto.getRank())
                    .article(articleProxy)
                    .rankingCount(dto.getRankingCount())
                    .build());
        }

        // 3. Fetch and prepare COMMENT ranking
        List<ArticleRankingDto> commentRankings = articleRepository.findTopArticlesByCommentCount(start, end);
        for (ArticleRankingDto dto : commentRankings) {
            Article articleProxy = articleRepository.getReferenceById(dto.getArticleId());
            rankingEntities.add(ArticleRanking.builder()
                    .rankingDate(targetDate)
                    .rankType("COMMENT")
                    .ranking(dto.getRank())
                    .article(articleProxy)
                    .rankingCount(dto.getRankingCount())
                    .build());
        }

        if (!rankingEntities.isEmpty()) {
            articleRankingRepository.saveAll(rankingEntities);
            log.info("Saved {} ranking entries for date {}", rankingEntities.size(), targetDate);
        }

        log.info("Successfully finished RankJobTasklet");
        return RepeatStatus.FINISHED;
    }
}
