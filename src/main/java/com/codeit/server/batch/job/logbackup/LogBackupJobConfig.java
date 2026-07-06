package com.codeit.server.batch.job.logbackup;

import com.codeit.server.batch.job.logbackup.tasklet.LogBackupTasklet;
import com.codeit.server.batch.monitoring.BatchMetricsJobExecutionListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class LogBackupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final LogBackupTasklet logBackupTasklet;
    private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

    @Bean
    public Job logBackupJob() {
        return new JobBuilder("logBackupJob", jobRepository)
                .listener(batchMetricsJobExecutionListener)
                .start(logBackupStep())
                .build();
    }

    @Bean
    public Step logBackupStep() {
        return new StepBuilder("logBackupStep", jobRepository)
                .tasklet(logBackupTasklet, transactionManager)
                .build();
    }
}
