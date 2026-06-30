package com.codeit.server.notification.service;

import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {

  CursorPageResponseNotificationDto getUnconfirmedNotifications(
      UUID userId, String cursor, String after, int limit
  );

  void confirmNotification(UUID userId, UUID notificationId);

  void confirmAllNotifications(UUID userId);

  void deleteOldConfirmedNotifications();

}
