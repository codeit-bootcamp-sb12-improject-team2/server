package com.codeit.server.notification.service;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.server.notification.dto.NotificationDto;
import com.codeit.server.notification.entity.Notification;
import com.codeit.server.notification.repository.NotificationRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  public CursorPageResponseNotificationDto getUnconfirmedNotifications(UUID userId, String cursor,
      String after, int limit) {

    Instant afterInstant = (after != null && !after.isBlank()) ? Instant.parse(after) : null;


    List<Notification> entities =
        notificationRepository.findUnconfirmedNotificationsByCursor(userId, cursor, afterInstant, limit);

    boolean hasNext = entities.size() > limit;
    List<Notification> resultPage = hasNext ? entities.subList(0, limit) : entities;

    List<NotificationDto> content = resultPage.stream()
        .map(this::convertToDto)
        .toList();

    Notification lastItem =
        (!resultPage.isEmpty() && hasNext) ? resultPage.get(resultPage.size() - 1) : null;

    String nextCursorStr = (lastItem != null) ? lastItem.getId().toString() : null;
    String nextAfterStr = (lastItem != null && lastItem.getCreatedAt() != null) ? lastItem.getCreatedAt().toString() : null;

    return CursorPageResponseNotificationDto.builder()
        .content(content)
        .nextCursor(nextCursorStr)
        .nextAfter(nextAfterStr)
        .size(content.size())
        .totalElements(notificationRepository.countUnconfirmedByUserId(userId))
        .hasNext(hasNext)
        .build();
  }

  @Override
  public void confirmNotification(UUID userId, UUID notificationId) {

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new BaseException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getUserId().equals(userId)) {
      throw new BaseException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    notificationRepository.updateConfirmStatus(notificationId, true);

  }

  @Override
  public void confirmAllNotifications(UUID userId) {

    notificationRepository.updateAllConfirmStatusByUserId(userId, true);

  }

  @Override
  public void deleteOldConfirmedNotifications() {

    Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
    notificationRepository.deleteConfirmedNotificationsOlderThan(oneWeekAgo);

  }

  @Override
  public void createNotification(UUID userId, String content, String resourceType, UUID resourceId) {
    Notification notification = Notification.builder()
        .id(UuidCreator.getTimeOrderedEpoch())
        .userId(userId)
        .content(content)
        .resourceType(resourceType)
        .resourceId(resourceId)
        .confirmed(false)
        .build();
    notificationRepository.save(notification);
  }

  private NotificationDto convertToDto(Notification entity) {
    return NotificationDto.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now())
        .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : Instant.now())
        .confirmed(entity.isConfirmed())
        .userId(entity.getUserId())
        .content(entity.getContent())
        .resourceType(entity.getResourceType())
        .resourceId(entity.getResourceId())
        .build();
  }

}

