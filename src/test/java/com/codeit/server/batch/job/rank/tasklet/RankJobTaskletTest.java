package com.codeit.server.batch.job.rank.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.article.entity.Article;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.rank.dto.ArticleRankingDto;
import com.codeit.server.rank.entity.ArticleRanking;
import com.codeit.server.rank.repository.ArticleRankingRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class RankJobTaskletTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleRankingRepository articleRankingRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    @InjectMocks
    private RankJobTasklet rankJobTasklet;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("성공 - targetDate가 지정된 경우 해당 일자의 VIEW 및 COMMENT 랭킹을 집계하고 저장한다")
    void success_withTargetDate() throws Exception {
        // Given
        String targetDateStr = "2026-07-06";
        LocalDate targetDate = LocalDate.parse(targetDateStr);
        Instant start = targetDate.atStartOfDay(KST).toInstant();
        Instant end = targetDate.plusDays(1).atStartOfDay(KST).toInstant();

        // 1. ChunkContext mock 설정
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getJobParameters()).thenReturn(Map.of("targetDate", targetDateStr));

        // 2. deleteByRankingDate mock 설정
        doNothing().when(articleRankingRepository).deleteByRankingDate(targetDate);

        // 3. findTopArticlesByViewCount mock 설정
        UUID viewArticleId = UUID.randomUUID();
        ArticleRankingDto viewDto = new ArticleRankingDto(
            1, viewArticleId, "VIEW인기기사", "언론사A", 100, 5, 100L, Instant.now()
        );
        when(articleRepository.findTopArticlesByViewCount(start, end)).thenReturn(List.of(viewDto));

        // 4. findTopArticlesByCommentCount mock 설정
        UUID commentArticleId = UUID.randomUUID();
        ArticleRankingDto commentDto = new ArticleRankingDto(
            1, commentArticleId, "COMMENT인기기사", "언론사B", 50, 20, 20L, Instant.now()
        );
        when(articleRepository.findTopArticlesByCommentCount(start, end)).thenReturn(List.of(commentDto));

        // 5. getReferenceById mock 설정
        Article viewArticleProxy = mock(Article.class);
        Article commentArticleProxy = mock(Article.class);
        when(articleRepository.getReferenceById(viewArticleId)).thenReturn(viewArticleProxy);
        when(articleRepository.getReferenceById(commentArticleId)).thenReturn(commentArticleProxy);

        // 6. saveAll mock 설정
        ArgumentCaptor<List<ArticleRanking>> rankingEntitiesCaptor = ArgumentCaptor.forClass(List.class);
        when(articleRankingRepository.saveAll(any())).thenReturn(List.of());

        // When
        RepeatStatus status = rankJobTasklet.execute(stepContribution, chunkContext);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        
        verify(articleRankingRepository).deleteByRankingDate(targetDate);
        verify(articleRepository).findTopArticlesByViewCount(start, end);
        verify(articleRepository).findTopArticlesByCommentCount(start, end);
        verify(articleRepository).getReferenceById(viewArticleId);
        verify(articleRepository).getReferenceById(commentArticleId);
        verify(articleRankingRepository).saveAll(rankingEntitiesCaptor.capture());

        List<ArticleRanking> savedRankings = rankingEntitiesCaptor.getValue();
        assertThat(savedRankings).hasSize(2);

        // VIEW 랭킹 검증
        ArticleRanking viewRanking = savedRankings.stream()
            .filter(r -> "VIEW".equals(r.getRankType()))
            .findFirst()
            .orElseThrow();
        assertThat(viewRanking.getRankingDate()).isEqualTo(targetDate);
        assertThat(viewRanking.getRanking()).isEqualTo(1);
        assertThat(viewRanking.getArticle()).isEqualTo(viewArticleProxy);
        assertThat(viewRanking.getRankingCount()).isEqualTo(100L);

        // COMMENT 랭킹 검증
        ArticleRanking commentRanking = savedRankings.stream()
            .filter(r -> "COMMENT".equals(r.getRankType()))
            .findFirst()
            .orElseThrow();
        assertThat(commentRanking.getRankingDate()).isEqualTo(targetDate);
        assertThat(commentRanking.getRanking()).isEqualTo(1);
        assertThat(commentRanking.getArticle()).isEqualTo(commentArticleProxy);
        assertThat(commentRanking.getRankingCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("성공 - targetDate 파라미터가 없는 경우 어제 날짜를 기준으로 랭킹을 집계하고 저장한다")
    void success_withoutTargetDate() throws Exception {
        // Given
        LocalDate yesterday = LocalDate.now(KST).minusDays(1);
        Instant start = yesterday.atStartOfDay(KST).toInstant();
        Instant end = yesterday.plusDays(1).atStartOfDay(KST).toInstant();

        // 1. ChunkContext mock 설정
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        // 파라미터 맵에 targetDate가 없을 때
        when(stepContext.getJobParameters()).thenReturn(Map.of());

        // 2. deleteByRankingDate mock 설정
        doNothing().when(articleRankingRepository).deleteByRankingDate(yesterday);

        // 3. findTopArticlesByViewCount & commentCount 빈 리스트 반환 설정
        when(articleRepository.findTopArticlesByViewCount(start, end)).thenReturn(List.of());
        when(articleRepository.findTopArticlesByCommentCount(start, end)).thenReturn(List.of());

        // When
        RepeatStatus status = rankJobTasklet.execute(stepContribution, chunkContext);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(articleRankingRepository).deleteByRankingDate(yesterday);
        verify(articleRepository).findTopArticlesByViewCount(start, end);
        verify(articleRepository).findTopArticlesByCommentCount(start, end);
    }
}
