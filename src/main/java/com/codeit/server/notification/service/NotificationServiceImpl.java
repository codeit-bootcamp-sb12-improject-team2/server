package com.codeit.server.notification.service;

import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.server.notification.dto.NotificationDto;
import com.codeit.server.notification.entity.Notification;
import com.codeit.server.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  public CursorPageResponseNotificationDto getUnconfirmedNotifications(UUID userId, UUID cursor,
      Instant after, int limit) {

    List<Notification> entities =
        notificationRepository.findUnconfirmedNotificationsByCursor(userId, cursor, after, limit);

    boolean hasNext = entities.size() > limit;
    List<Notification> resultPage = hasNext ? entities.subList(0, limit) : entities;

    List<NotificationDto> content = resultPage.stream()
        .map(this::convertToDto)
        .toList();

    Notification lastItem =
        (!resultPage.isEmpty() && hasNext) ? resultPage.get(resultPage.size() - 1) : null;
    UUID nextCursor = (lastItem != null) ? lastItem.getId() : null;
    Instant nextAfter = (lastItem != null) ? lastItem.getCreatedAt() : null;

    return CursorPageResponseNotificationDto.builder()
        .content(content)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(content.size())
        .totalElements(notificationRepository.countUnconfirmedByUserId(userId)) // 인라인 처리
        .hasNext(hasNext)
        .build();
  }

  private NotificationDto convertToDto(Notification entity) {
    return NotificationDto.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .confirmed(entity.isConfirmed())
        .userId(entity.getUserId())
        .content(entity.getContent())
        .resourceType(entity.getResourceType())
        .resourceId(entity.getResourceId())
        .build();
  }
}

