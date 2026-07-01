package com.codeit.server.batch.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchMetricsJobExecutionListener implements JobExecutionListener {

    private final BatchMetrics batchMetrics;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting batch job: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();
        String statusStr = status != null ? status.name() : "UNKNOWN";

        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long durationMs = 0;
        if (startTime != null && endTime != null) {
            durationMs = Duration.between(startTime, endTime).toMillis();
        }

        log.info("Finished batch job: {} with status: {}. Duration: {} ms", jobName, statusStr, durationMs);

        // Record custom metrics
        batchMetrics.incrementJobCount(jobName, statusStr);
        batchMetrics.recordJobDuration(jobName, statusStr, durationMs);
    }
}
