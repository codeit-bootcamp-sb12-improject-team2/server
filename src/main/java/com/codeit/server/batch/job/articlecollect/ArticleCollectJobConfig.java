package com.codeit.server.batch.job.articlecollect;

import com.codeit.server.batch.job.articlecollect.tasklet.ArticleCollectTasklet;
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
public class ArticleCollectJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleCollectTasklet articleCollectTasklet;
    private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

    @Bean
    public Job articleCollectJob() {
        return new JobBuilder("articleCollectJob", jobRepository)
                .listener(batchMetricsJobExecutionListener)
                .start(articleCollectStep())
                .build();
    }

    @Bean
    public Step articleCollectStep() {
        return new StepBuilder("articleCollectStep", jobRepository)
                .tasklet(articleCollectTasklet, transactionManager)
                .build();
    }
}
