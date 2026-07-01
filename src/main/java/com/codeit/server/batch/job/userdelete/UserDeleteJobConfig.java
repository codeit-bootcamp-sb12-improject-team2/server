package com.codeit.server.batch.job.userdelete;

import com.codeit.server.batch.job.userdelete.tasklet.UserDeleteTasklet;
import com.codeit.server.batch.monitoring.BatchMetricsJobExecutionListener;
import com.codeit.server.user.repository.UserRepository;
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
public class UserDeleteJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final UserRepository userRepository;

  @Bean(name = "userDeleteJob")
  public Job userDeleteJob(Step userDeleteStep) {
    return new JobBuilder("userDeleteJob", jobRepository)
        .start(userDeleteStep)
        .build();
  }

  @Bean
  public Step userDeleteStep(UserDeleteTasklet userDeleteTasklet) {
    return new StepBuilder("userDeleteStep", jobRepository)
        .tasklet(userDeleteTasklet, transactionManager)
        .listener(userDeleteTasklet)
        .build();
  }

  @Bean
  public UserDeleteTasklet userDeleteTasklet() {
    return new UserDeleteTasklet(userRepository);
  }
}
