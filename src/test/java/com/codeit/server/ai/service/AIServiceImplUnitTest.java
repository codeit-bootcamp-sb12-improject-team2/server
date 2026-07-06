package com.codeit.server.ai.service;

import com.codeit.server.ai.client.GeminiClient;
import com.codeit.server.ai.client.NewsCrawler;
import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceImplUnitTest {

    @Mock
    private NewsCrawler newsCrawler;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private AIServiceImpl aiService;

    @Test
    void summarizeNews_whenArticleNotFound_shouldThrowRuntimeException() {
        // Given
        UUID articleId = UUID.randomUUID();
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> aiService.summarizeNews(articleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("기사 없음");

        verifyNoInteractions(newsCrawler);
        verifyNoInteractions(geminiClient);
    }

    @Test
    void summarizeNews_whenArticleExists_shouldCrawlAndSummarize() {
        // Given
        UUID articleId = UUID.randomUUID();
        Article article = mock(Article.class);
        when(article.getSourceUrl()).thenReturn("http://example.com/news");
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        String crawledContent = "This is crawled content of the news.";
        when(newsCrawler.crawl("http://example.com/news")).thenReturn(crawledContent);

        NewsSummaryResponseDto expectedDto = new NewsSummaryResponseDto("요약된 기사 내용입니다.", List.of("키워드1", "키워드2"));
        when(geminiClient.summarizeNews(crawledContent)).thenReturn(expectedDto);

        // When
        NewsSummaryResponseDto actualDto = aiService.summarizeNews(articleId);

        // Then
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getSummary()).isEqualTo("요약된 기사 내용입니다.");
        assertThat(actualDto.getKeywords()).containsExactly("키워드1", "키워드2");

        verify(articleRepository).findById(articleId);
        verify(newsCrawler).crawl("http://example.com/news");
        verify(geminiClient).summarizeNews(crawledContent);
    }
}
