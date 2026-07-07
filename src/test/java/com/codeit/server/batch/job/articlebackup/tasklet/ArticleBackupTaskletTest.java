package com.codeit.server.batch.job.articlebackup.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class ArticleBackupTaskletTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ArticleBackupTasklet articleBackupTasklet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleBackupTasklet, "bucket", "test-bucket");
        ReflectionTestUtils.setField(articleBackupTasklet, "prefix", "article-backups");
    }

    @Nested
    @DisplayName("execute 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("성공 - 백업 기사를 JSON으로 직렬화하여 S3에 업로드한다")
        void success_backupArticlesToS3() throws Exception {

            // Given
            List<ArticleBackupDto> articles = List.of(
                    createBackupDto("https://news.test/backup-1")
            );

            when(articleRepository.findBackup(any(), any()))
                    .thenReturn(articles);

            when(objectMapper.writeValueAsString(articles))
                    .thenReturn("[{}]");

            // When
            RepeatStatus result =
                    articleBackupTasklet.execute(null, null);

            // Then
            assertThat(result)
                    .isEqualTo(RepeatStatus.FINISHED);

            verify(articleRepository)
                    .findBackup(any(), any());

            verify(objectMapper)
                    .writeValueAsString(articles);

            ArgumentCaptor<PutObjectRequest> requestCaptor =
                    ArgumentCaptor.forClass(PutObjectRequest.class);

            verify(s3Client)
                    .putObject(requestCaptor.capture(), any(RequestBody.class));

            PutObjectRequest request = requestCaptor.getValue();

            assertThat(request.bucket())
                    .isEqualTo("test-bucket");

            assertThat(request.key())
                    .startsWith("article-backups/")
                    .endsWith("/articles.json");

            assertThat(request.contentType())
                    .isEqualTo("application/json");
        }

        @Test
        @DisplayName("성공 - 백업 대상이 없어도 빈 JSON을 업로드한다")
        void success_backupEmptyArticles() throws Exception {

            // Given

            when(articleRepository.findBackup(any(), any()))
                    .thenReturn(List.of());

            when(objectMapper.writeValueAsString(List.of()))
                    .thenReturn("[]");

            // When

            RepeatStatus result =
                    articleBackupTasklet.execute(null, null);

            // Then

            assertThat(result)
                    .isEqualTo(RepeatStatus.FINISHED);

            verify(s3Client)
                    .putObject(any(PutObjectRequest.class),
                            any(RequestBody.class));

        }

        @Test
        @DisplayName("실패 - JSON 직렬화 실패")
        void fail_jsonSerialize() throws Exception {

            // Given

            List<ArticleBackupDto> articles = List.of(
                    createBackupDto("https://news.test/backup-1")
            );

            when(articleRepository.findBackup(any(), any()))
                    .thenReturn(articles);

            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new RuntimeException("json error"));

            // When & Then

            assertThatThrownBy(() ->
                    articleBackupTasklet.execute(null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("json error");

        }

        @Test
        @DisplayName("실패 - S3 업로드 실패")
        void fail_uploadS3() throws Exception {

            // Given

            List<ArticleBackupDto> articles = List.of(
                    createBackupDto("https://news.test/backup-1")
            );

            when(articleRepository.findBackup(any(), any()))
                    .thenReturn(articles);

            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("[{}]");

            doThrow(new RuntimeException("s3 error"))
                    .when(s3Client)
                    .putObject(any(PutObjectRequest.class),
                            any(RequestBody.class));

            // When & Then

            assertThatThrownBy(() ->
                    articleBackupTasklet.execute(null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("s3 error");

        }

    }

    private ArticleBackupDto createBackupDto(String sourceUrl) {

        return ArticleBackupDto.builder()
                .id(UUID.randomUUID())
                .source("NAVER")
                .sourceUrl(sourceUrl)
                .title("백업 기사")
                .publishDate(Instant.parse("2026-07-06T00:00:00Z"))
                .summary("백업 기사 요약")
                .viewCount(10)
                .commentCount(2)
                .createdAt(Instant.parse("2026-07-06T01:00:00Z"))
                .updatedAt(Instant.parse("2026-07-06T02:00:00Z"))
                .deleted(false)
                .build();

    }

}