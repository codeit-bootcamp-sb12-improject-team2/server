package com.codeit.server.batch.job.articlecollect;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCollectScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleCollectJob;

    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각
//    @Scheduled(cron = "0 * * * * *") // 매 분 for test
    public void runArticleCollectJob() throws Exception {
        jobLauncher.run(
                articleCollectJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );
    }
}