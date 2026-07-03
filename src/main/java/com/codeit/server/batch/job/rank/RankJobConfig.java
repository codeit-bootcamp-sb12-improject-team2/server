package com.codeit.server.batch.job.rank;

import com.codeit.server.batch.job.rank.tasklet.RankJobTasklet;
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
public class RankJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RankJobTasklet rankJobTasklet;
    private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

    @Bean
    public Job rankJob() {
        return new JobBuilder("rankJob", jobRepository)
                .listener(batchMetricsJobExecutionListener)
                .start(rankStep())
                .build();
    }

    @Bean
    public Step rankStep() {
        return new StepBuilder("rankStep", jobRepository)
                .tasklet(rankJobTasklet, transactionManager)
                .build();
    }
}
