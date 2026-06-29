package com.codeit.server.notification.dto;


import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorPageResponseNotificationDto {

  private final List<NotificationDto> content;
  private final UUID nextCursor;
  private final Instant nextAfter;
  private final int size;
  private final long totalElements;
  private final boolean hasNext;

}
