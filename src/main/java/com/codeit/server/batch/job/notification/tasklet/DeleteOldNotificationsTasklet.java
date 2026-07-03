package com.codeit.server.batch.job.notification.tasklet;

import com.codeit.server.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteOldNotificationsTasklet implements Tasklet {

    private final NotificationRepository notificationRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>>>> Starting DeleteOldNotificationsTasklet");

        // 1주일(7일) 경과 기준일 구하기 (현재 시각 기준 7일 전)
        Instant thresholdDate = Instant.now().minus(7, ChronoUnit.DAYS);
        log.info("Deleting confirmed notifications older than threshold date: {}", thresholdDate);

        // 삭제 쿼리 실행
        notificationRepository.deleteConfirmedNotificationsOlderThan(thresholdDate);

        log.info("Successfully finished DeleteOldNotificationsTasklet");
        return RepeatStatus.FINISHED;
    }
}
