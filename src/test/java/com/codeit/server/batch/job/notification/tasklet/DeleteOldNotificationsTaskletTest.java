package com.codeit.server.batch.job.notification.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.codeit.server.notification.repository.NotificationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class DeleteOldNotificationsTaskletTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @InjectMocks
    private DeleteOldNotificationsTasklet deleteOldNotificationsTasklet;

    @Test
    @DisplayName("성공 - 7일 경과된 확인된 알림을 삭제하고 FINISHED를 리턴한다")
    void success() throws Exception {
        // Given
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        doNothing().when(notificationRepository).deleteConfirmedNotificationsOlderThan(any(Instant.class));

        // When
        RepeatStatus status = deleteOldNotificationsTasklet.execute(stepContribution, chunkContext);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(notificationRepository).deleteConfirmedNotificationsOlderThan(instantCaptor.capture());
        
        Instant captured = instantCaptor.getValue();
        Instant now = Instant.now();
        
        assertThat(captured).isBefore(now);
        assertThat(ChronoUnit.DAYS.between(captured, now)).isEqualTo(7);
    }
}
