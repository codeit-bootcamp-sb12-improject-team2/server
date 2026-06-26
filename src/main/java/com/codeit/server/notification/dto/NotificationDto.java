package com.codeit.server.notification.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDto {

  private final UUID id;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final boolean confirmed;
  private final UUID userId;
  private final String content;
  private final String resourceType;
  private final UUID resourceId;

}
