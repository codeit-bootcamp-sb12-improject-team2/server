package com.codeit.server.notification.repository;

import com.codeit.server.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationQueryRepository {

  List<Notification> findUnconfirmedNotificationsByCursor(
      UUID userId, UUID cursorId, Instant afterInstant, int limit);

  long countUnconfirmedByUserId(UUID userId);

}
