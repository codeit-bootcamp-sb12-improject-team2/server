package com.codeit.server.batch.job.notification;

import com.codeit.server.batch.job.notification.tasklet.DeleteOldNotificationsTasklet;
import com.codeit.server.batch.monitoring.BatchMetricsJobExecutionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeleteOldNotificationsJobConfig {

    private final DeleteOldNotificationsTasklet deleteOldNotificationsTasklet;
    private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

    @Bean
    public Job deleteOldNotificationsJob(JobRepository jobRepository, Step deleteOldNotificationsStep) {
        return new JobBuilder("deleteOldNotificationsJob", jobRepository)
                .start(deleteOldNotificationsStep)
                .listener(batchMetricsJobExecutionListener)
                .build();
    }

    @Bean
    public Step deleteOldNotificationsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteOldNotificationsStep", jobRepository)
                .tasklet(deleteOldNotificationsTasklet, transactionManager)
                .build();
    }
}
