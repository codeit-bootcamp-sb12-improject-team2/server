package com.codeit.server.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("deleteOldNotificationsJob")
    private final Job deleteOldNotificationsJob;

    @Qualifier("userDeleteJob")
    private final Job userDeleteJob;

    @Qualifier("articleCollectJob")
    private final Job articleCollectJob;


    @Scheduled(cron = "${spring.batch.scheduler.cron.delete-old-notifications:0 0 0 * * ?}")
    public void runDeleteOldNotificationsJob() {
        log.info("Scheduled task triggered: runDeleteOldNotificationsJob");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(deleteOldNotificationsJob, jobParameters);
            log.info("Scheduled deleteOldNotificationsJob completed successfully.");
        } catch (Exception e) {
            log.error("Failed to execute scheduled deleteOldNotificationsJob", e);
        }
    }

    @Scheduled(cron = "${spring.batch.scheduler.cron.delete-expired-users:0 0 1 * * *}")
    public void runUserDeleteJob() {
        log.info("Scheduled task triggered: runUserDeleteJob");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(userDeleteJob, jobParameters);
            log.info("Scheduled userDeleteJob completed successfully.");
        } catch (Exception e) {
            log.error("Failed to execute scheduled userDeleteJob", e);
        }
    }

    @Scheduled(cron = "${spring.batch.scheduler.cron.collect-article:0 0 * * * *}")
    public void runArticleCollectJob() {
        log.info("Scheduled task triggered: runArticleCollectJob");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(articleCollectJob, jobParameters);

            log.info("Scheduled articleCollectJob completed successfully.");
        } catch (Exception e) {
            log.error("Failed to execute scheduled articleCollectJob", e);
        }
    }
}

