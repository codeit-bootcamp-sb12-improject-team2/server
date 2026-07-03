package com.codeit.server.batch.job.articlebackup;

import com.codeit.server.batch.job.articlebackup.tasklet.ArticleBackupTasklet;
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
public class ArticleBackupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleBackupTasklet articleBackupTasklet;
    private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

    @Bean
    public Job articleBackupJob() {
        return new JobBuilder("articleBackupJob", jobRepository)
                .listener(batchMetricsJobExecutionListener)
                .start(articleBackupStep())
                .build();
    }

    @Bean
    public Step articleBackupStep() {
        return new StepBuilder("articleBackupStep", jobRepository)
                .tasklet(articleBackupTasklet, transactionManager)
                .build();
    }

}