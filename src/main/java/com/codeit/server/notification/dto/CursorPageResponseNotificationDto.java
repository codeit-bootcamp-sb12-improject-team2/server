package com.codeit.server.notification.dto;


import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorPageResponseNotificationDto {

  private final List<NotificationDto> content;
  private final String nextCursor;
  private final String nextAfter;
  private final int size;
  private final long totalElements;
  private final boolean hasNext;

}
