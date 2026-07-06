package com.codeit.server.batch.job.logbackup.tasklet;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
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
public class LogBackupTasklet implements Tasklet {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.log-backup-prefix:logs}")
    private String prefix;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>>>> Starting LogBackupTasklet");

        // 어제 날짜의 로그 백업 대상 설정
        LocalDate targetDate = LocalDate.now(KST).minusDays(1);
        String logFileName = "app-" + targetDate + ".log";
        File logFile = new File("logs", logFileName);

        log.info("Log backup target date={}, file path={}", targetDate, logFile.getAbsolutePath());

        if (!logFile.exists() || !logFile.isFile()) {
            log.warn("Target log file '{}' does not exist. It might have not been rolled yet or no logs were written yesterday.", logFile.getName());
            return RepeatStatus.FINISHED;
        }

        String key = prefix + "/" + targetDate + "/app.log";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("text/plain")
                .build();

        s3Client.putObject(request, RequestBody.fromFile(logFile));

        log.info("Uploaded log file to S3. bucket={}, key={}, size={} bytes", bucket, key, logFile.length());
        log.info("Successfully finished LogBackupTasklet");

        return RepeatStatus.FINISHED;
    }
}
