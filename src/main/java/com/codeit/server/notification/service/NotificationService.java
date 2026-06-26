package com.codeit.server.notification.service;

import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {

  CursorPageResponseNotificationDto getUnconfirmedNotifications(
      UUID userId, String cursor, Instant after, int limit
  );

}
