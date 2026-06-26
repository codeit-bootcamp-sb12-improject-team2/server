package com.codeit.server.notification.service;

import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

  @Override
  public CursorPageResponseNotificationDto getUnconfirmedNotifications(UUID userId, String cursor,
      Instant after, int limit) {
    return null;
  }
}
