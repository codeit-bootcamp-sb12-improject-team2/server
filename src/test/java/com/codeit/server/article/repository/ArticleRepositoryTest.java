package com.codeit.server.article.repository;

import com.codeit.server.article.entity.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.codeit.server.global.config.QuerydslConfig;
import com.codeit.server.global.config.JpaAuditingConfig;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, ArticleRepositoryImpl.class, JpaAuditingConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void testSaveAndFindBySourceUrl() {
        // Given
        Article article = Article.builder()
                .source("네이버뉴스")
                .sourceUrl("http://naver.com/test-url-123")
                .title("테스트 뉴스 제목")
                .summary("요약 내용")
                .publishDate(Instant.now())
                .build();

        articleRepository.save(article);

        // When
        Optional<Article> found = articleRepository.findBySourceUrl("http://naver.com/test-url-123");
        boolean exists = articleRepository.existsBySourceUrl("http://naver.com/test-url-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 뉴스 제목");
        assertThat(found.get().getSource()).isEqualTo("네이버뉴스");
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsBySourceUrl_whenNotExists_shouldReturnFalse() {
        // When
        boolean exists = articleRepository.existsBySourceUrl("http://non-existing-url.com");

        // Then
        assertThat(exists).isFalse();
    }
}
