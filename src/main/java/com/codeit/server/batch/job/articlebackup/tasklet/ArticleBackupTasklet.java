package com.codeit.server.batch.job.articlebackup.tasklet;

import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleBackupTasklet implements Tasklet {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul"); // 국내 언론사만 유지중이라 KST 사용, 추후 다른 해외 언론추가시 변환 고려

    private final ArticleRepository articleRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.article-backup-prefix}")
    private String prefix;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>>>> Starting ArticleBackupTasklet");

        LocalDate backupDate = LocalDate.now(KST);

        Instant start = backupDate.atStartOfDay(KST).toInstant();
        Instant end = backupDate.plusDays(1).atStartOfDay(KST).toInstant();

        log.info("Article backup target date={}, start={}, end={}", backupDate, start, end);

        List<ArticleBackupDto> articles = articleRepository.findBackup(start, end);

        log.info("Found {} articles to backup.", articles.size());

        String key = prefix + "/" + backupDate + "/articles.json";

        String json = objectMapper.writeValueAsString(articles);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(request, RequestBody.fromString(json));

        log.info("Uploaded article backup to S3. bucket={}, key={}, count={}", bucket, key, articles.size());
        log.info("Successfully finished ArticleBackupTasklet");

        return RepeatStatus.FINISHED;
    }
}