package com.codeit.server.notification.service;

import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {

  CursorPageResponseNotificationDto getUnconfirmedNotifications(
      UUID userId, UUID cursor, Instant after, int limit
  );

  void confirmNotification(UUID userId, UUID notificationId);

  void confirmAllNotifications(UUID userId);

  void deleteOldConfirmedNotifications();

}
