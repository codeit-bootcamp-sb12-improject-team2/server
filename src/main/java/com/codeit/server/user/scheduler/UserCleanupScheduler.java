package com.codeit.server.user.scheduler;

import com.codeit.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
  private final UserService userService;

  @Scheduled(cron = "0 0 0 * * *")
  public void purgeExpiredSoftDeletedUsers() {
    log.info("논리 삭제 사용자 물리 삭제 스케줄러 시작");

    userService.purgeExpiredSoftDeletedUsers();

    log.info("논리 삭제 사용자 물리 삭제 스케줄러 종료");
  }
}
