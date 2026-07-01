package com.codeit.server.batch.job.userdelete.tasklet;

import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class UserDeleteTasklet implements Tasklet, StepExecutionListener {

  private final UserRepository userRepository;

  @Override
  public void beforeStep(StepExecution stepExecution) {
    log.info("[UserDeleteTasklet] 시작");
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);

    List<User> expiredUsers = userRepository.findAllSoftDeletedBefore(threshold);

    userRepository.deleteAll(expiredUsers);

    contribution.incrementWriteCount(expiredUsers.size());

    log.info("[UserDeleteTasklet] 물리 삭제 완료: {}명", expiredUsers.size());

    return RepeatStatus.FINISHED;
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    log.info("[UserDeleteTasklet] 종료");
    return ExitStatus.COMPLETED;
  }
}
