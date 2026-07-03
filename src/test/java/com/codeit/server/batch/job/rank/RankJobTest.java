package com.codeit.server.batch.job.rank;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.server.article.entity.Article;
import com.codeit.server.rank.entity.ArticleRanking;
import com.codeit.server.article.entity.ArticleView;
import com.codeit.server.rank.repository.ArticleRankingRepository;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.article.repository.ArticleViewRepository;
import com.codeit.server.comment.entity.Comment;
import com.codeit.server.comment.repository.CommentRepository;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("rankJob")
    private Job rankJob;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleRankingRepository articleRankingRepository;

    @Autowired
    private ArticleViewRepository articleViewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private final List<Article> testArticles = new ArrayList<>();
    private final List<ArticleView> testViews = new ArrayList<>();
    private final List<Comment> testComments = new ArrayList<>();
    private final ZoneId KST = ZoneId.of("Asia/Seoul");
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        targetDate = LocalDate.now(KST);

        // Proactive cleanup of leaked test data from previous runs to prevent DB constraint errors
        articleRankingRepository.deleteByRankingDate(targetDate);
        List<String> urls = List.of("https://test.news.a/1", "https://test.news.b/2", "https://test.news.c/3");
        for (String url : urls) {
            articleRepository.findBySourceUrl(url).ifPresent(art -> {
                articleViewRepository.findAll().stream()
                        .filter(v -> v.getArticleId().equals(art.getId()))
                        .forEach(articleViewRepository::delete);
                commentRepository.findAll().stream()
                        .filter(c -> c.getArticleId().equals(art.getId()))
                        .forEach(commentRepository::delete);
                articleRepository.delete(art);
            });
        }
        
        userRepository.findAll().stream()
                .filter(u -> "test-user@codeit.com".equals(u.getEmail()))
                .forEach(userRepository::delete);

        // Create and save test user
        User user = User.builder()
                .email("test-user@codeit.com")
                .nickname("Test User")
                .password("password123")
                .isDeleted(false)
                .build();
        testUser = userRepository.save(user);

        Instant publishInstant = targetDate.atStartOfDay(KST).plusHours(12).toInstant();

        // Create and save test articles
        Article articleA = Article.builder()
                .source("Test News A")
                .sourceUrl("https://test.news.a/1")
                .title("Test Article A")
                .summary("Summary A")
                .publishDate(publishInstant)
                .build();

        Article articleB = Article.builder()
                .source("Test News B")
                .sourceUrl("https://test.news.b/2")
                .title("Test Article B")
                .summary("Summary B")
                .publishDate(publishInstant)
                .build();

        Article articleC = Article.builder()
                .source("Test News C")
                .sourceUrl("https://test.news.c/3")
                .title("Test Article C")
                .summary("Summary C")
                .publishDate(publishInstant)
                .build();

        testArticles.add(articleRepository.save(articleA));
        testArticles.add(articleRepository.save(articleB));
        testArticles.add(articleRepository.save(articleC));

        // Save 10 views for Article A
        for (int i = 0; i < 10; i++) {
            ArticleView view = ArticleView.builder()
                    .articleId(testArticles.get(0).getId())
                    .userId(testUser.getId())
                    .build();
            testViews.add(articleViewRepository.save(view));
        }

        // Save 20 views for Article B
        for (int i = 0; i < 20; i++) {
            ArticleView view = ArticleView.builder()
                    .articleId(testArticles.get(1).getId())
                    .userId(testUser.getId())
                    .build();
            testViews.add(articleViewRepository.save(view));
        }

        // Save 5 views for Article C
        for (int i = 0; i < 5; i++) {
            ArticleView view = ArticleView.builder()
                    .articleId(testArticles.get(2).getId())
                    .userId(testUser.getId())
                    .build();
            testViews.add(articleViewRepository.save(view));
        }

        // Save 5 comments for Article A
        for (int i = 0; i < 5; i++) {
            Comment comment = Comment.builder()
                    .articleId(testArticles.get(0).getId())
                    .userId(testUser.getId())
                    .content("Comment A " + i)
                    .build();
            testComments.add(commentRepository.save(comment));
        }

        // Save 2 comments for Article B
        for (int i = 0; i < 2; i++) {
            Comment comment = Comment.builder()
                    .articleId(testArticles.get(1).getId())
                    .userId(testUser.getId())
                    .content("Comment B " + i)
                    .build();
            testComments.add(commentRepository.save(comment));
        }

        // Save 8 comments for Article C
        for (int i = 0; i < 8; i++) {
            Comment comment = Comment.builder()
                    .articleId(testArticles.get(2).getId())
                    .userId(testUser.getId())
                    .content("Comment C " + i)
                    .build();
            testComments.add(commentRepository.save(comment));
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up rankings for target date
        articleRankingRepository.deleteByRankingDate(targetDate);
        
        // Clean up comments and views referencing test articles
        commentRepository.deleteAll(testComments);
        articleViewRepository.deleteAll(testViews);
        
        // Clean up test articles
        articleRepository.deleteAll(testArticles);

        // Clean up test user
        if (testUser != null) {
            userRepository.delete(testUser);
        }
    }

    @Test
    void testRankJob() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDate.toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncher.run(rankJob, jobParameters);

        // Then
        if (jobExecution.getStatus().isUnsuccessful()) {
            System.err.println("Job failed with exceptions: " + jobExecution.getAllFailureExceptions());
        }
        assertThat(jobExecution.getStatus().isUnsuccessful()).isFalse();

        // Verify VIEW Rankings
        List<ArticleRanking> viewRankings = articleRankingRepository
                .findAll().stream()
                .filter(r -> r.getRankingDate().equals(targetDate) && r.getRankType().equals("VIEW"))
                .sorted((r1, r2) -> Integer.compare(r1.getRanking(), r2.getRanking()))
                .toList();

        assertThat(viewRankings).hasSize(3);
        // Rank 1: Article B (viewCount = 20)
        assertThat(viewRankings.get(0).getArticle().getId()).isEqualTo(testArticles.get(1).getId());
        assertThat(viewRankings.get(0).getRankingCount()).isEqualTo(20);
        // Rank 2: Article A (viewCount = 10)
        assertThat(viewRankings.get(1).getArticle().getId()).isEqualTo(testArticles.get(0).getId());
        assertThat(viewRankings.get(1).getRankingCount()).isEqualTo(10);
        // Rank 3: Article C (viewCount = 5)
        assertThat(viewRankings.get(2).getArticle().getId()).isEqualTo(testArticles.get(2).getId());
        assertThat(viewRankings.get(2).getRankingCount()).isEqualTo(5);

        // Verify COMMENT Rankings
        List<ArticleRanking> commentRankings = articleRankingRepository
                .findAll().stream()
                .filter(r -> r.getRankingDate().equals(targetDate) && r.getRankType().equals("COMMENT"))
                .sorted((r1, r2) -> Integer.compare(r1.getRanking(), r2.getRanking()))
                .toList();

        assertThat(commentRankings).hasSize(3);
        // Rank 1: Article C (commentCount = 8)
        assertThat(commentRankings.get(0).getArticle().getId()).isEqualTo(testArticles.get(2).getId());
        assertThat(commentRankings.get(0).getRankingCount()).isEqualTo(8);
        // Rank 2: Article A (commentCount = 5)
        assertThat(commentRankings.get(1).getArticle().getId()).isEqualTo(testArticles.get(0).getId());
        assertThat(commentRankings.get(1).getRankingCount()).isEqualTo(5);
        // Rank 3: Article B (commentCount = 2)
        assertThat(commentRankings.get(2).getArticle().getId()).isEqualTo(testArticles.get(1).getId());
        assertThat(commentRankings.get(2).getRankingCount()).isEqualTo(2);
    }
}
