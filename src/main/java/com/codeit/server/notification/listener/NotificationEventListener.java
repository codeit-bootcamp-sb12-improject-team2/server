package com.codeit.server.notification.listener;

import com.codeit.server.notification.event.NotificationEvent;
import com.codeit.server.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @Async("notificationAsyncExecutor")
  @EventListener
  public void handleNotificationEvent(NotificationEvent event) {
    log.info("Received notification event for user: {}, content: {}", event.userId(), event.content());
    try {
      notificationService.createNotification(
          event.userId(),
          event.content(),
          event.resourceType(),
          event.resourceId()
      );
    } catch (Exception e) {
      log.error("Failed to process notification event: {}", event, e);
    }
  }
}
