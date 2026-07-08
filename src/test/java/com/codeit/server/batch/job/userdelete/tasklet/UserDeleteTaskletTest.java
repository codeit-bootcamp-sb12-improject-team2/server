package com.codeit.server.batch.job.userdelete.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
public class UserDeleteTaskletTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private StepContribution stepContribution;

  @Mock
  private ChunkContext chunkContext;

  @InjectMocks
  private UserDeleteTasklet userDeleteTasklet;

  @Test
  @DisplayName("성공 - 논리 삭제 후 1일이 지난 사용자를 물리 삭제하고 FINISHED를 반환한다")
  void success() throws Exception {
    // Given
    User user1 = User.builder()
        .email("test1@example.com")
        .nickname("testUser1")
        .password("password123")
        .isDeleted(true)
        .build();

    User user2 = User.builder()
        .email("test2@example.com")
        .nickname("testUser2")
        .password("password123")
        .isDeleted(true)
        .build();

    List<User> expiredUsers = List.of(user1, user2);

    ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);

    when(userRepository.findAllSoftDeletedBefore(any(Instant.class)))
        .thenReturn(expiredUsers);

    // When
    RepeatStatus status = userDeleteTasklet.execute(stepContribution, chunkContext);

    // Then
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);

    verify(userRepository).findAllSoftDeletedBefore(instantCaptor.capture());
    verify(userRepository).deleteAll(expiredUsers);
    verify(stepContribution).incrementWriteCount(expiredUsers.size());

    Instant capturedThreshold = instantCaptor.getValue();
    Duration duration = Duration.between(capturedThreshold, Instant.now());

    assertThat(duration.toHours()).isBetween(23L, 24L);
  }

  @Test
  @DisplayName("성공 - beforeStep 실행")
  void beforeStep_success() {
    StepExecution stepExecution = mock(StepExecution.class);

    userDeleteTasklet.beforeStep(stepExecution);
  }

  @Test
  @DisplayName("성공 - afterStep 실행")
  void afterStep_success() {
    StepExecution stepExecution = mock(StepExecution.class);

    ExitStatus result = userDeleteTasklet.afterStep(stepExecution);

    assertThat(result).isEqualTo(ExitStatus.COMPLETED);
  }
}
