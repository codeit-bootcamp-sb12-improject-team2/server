package com.codeit.server.notification.listener;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.codeit.server.notification.event.NotificationEvent;
import com.codeit.server.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NotificationEventListenerTest {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  void testNotificationEventListenerHandlesEvent() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID resourceId = UUID.randomUUID();
    String content = "Hello world";
    String resourceType = "BUDGET";
    NotificationEvent event = new NotificationEvent(userId, content, resourceType, resourceId);

    // When
    eventPublisher.publishEvent(event);

    // Then
    verify(notificationService, timeout(2000)).createNotification(userId, content, resourceType, resourceId);
  }
}
