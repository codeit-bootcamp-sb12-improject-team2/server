package com.codeit.server.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.server.notification.entity.Notification;
import com.codeit.server.notification.repository.NotificationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  @Nested
  @DisplayName("getUnconfirmedNotifications 테스트")
  class GetUnconfirmedNotifications {

    @Test
    @DisplayName("성공 - 다음 페이지가 있는 경우 hasNext가 true여야 한다")
    void success_hasNext() {
      // Given
      UUID userId = UUID.randomUUID();
      String cursor = "cursor-id";
      String after = "2026-07-06T00:00:00Z";
      Instant afterInstant = Instant.parse(after);
      int limit = 2;

      Notification n1 = Notification.builder()
          .id(UUID.randomUUID())
          .userId(userId)
          .content("content1")
          .confirmed(false)
          .createdAt(Instant.now().minusSeconds(10))
          .build();

      Notification n2 = Notification.builder()
          .id(UUID.randomUUID())
          .userId(userId)
          .content("content2")
          .confirmed(false)
          .createdAt(Instant.now().minusSeconds(20))
          .build();

      Notification n3 = Notification.builder()
          .id(UUID.randomUUID())
          .userId(userId)
          .content("content3")
          .confirmed(false)
          .createdAt(Instant.now().minusSeconds(30))
          .build();

      when(notificationRepository.findUnconfirmedNotificationsByCursor(userId, cursor, afterInstant, limit))
          .thenReturn(List.of(n1, n2, n3));
      when(notificationRepository.countUnconfirmedByUserId(userId))
          .thenReturn(10L);

      // When
      CursorPageResponseNotificationDto result = notificationService.getUnconfirmedNotifications(userId, cursor, after, limit);

      // Then
      assertThat(result.isHasNext()).isTrue();
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getNextCursor()).isEqualTo(n2.getId().toString());
      assertThat(result.getNextAfter()).isEqualTo(n2.getCreatedAt().toString());
      assertThat(result.getTotalElements()).isEqualTo(10L);
    }

    @Test
    @DisplayName("성공 - 다음 페이지가 없는 경우 hasNext가 false여야 한다")
    void success_noNext() {
      // Given
      UUID userId = UUID.randomUUID();
      int limit = 5;

      Notification n1 = Notification.builder()
          .id(UUID.randomUUID())
          .userId(userId)
          .content("content1")
          .confirmed(false)
          .createdAt(Instant.now().minusSeconds(10))
          .build();

      when(notificationRepository.findUnconfirmedNotificationsByCursor(userId, null, null, limit))
          .thenReturn(List.of(n1));
      when(notificationRepository.countUnconfirmedByUserId(userId))
          .thenReturn(1L);

      // When
      CursorPageResponseNotificationDto result = notificationService.getUnconfirmedNotifications(userId, null, null, limit);

      // Then
      assertThat(result.isHasNext()).isFalse();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getNextCursor()).isNull();
      assertThat(result.getNextAfter()).isNull();
      assertThat(result.getTotalElements()).isEqualTo(1L);
    }
  }

  @Nested
  @DisplayName("confirmNotification 테스트")
  class ConfirmNotification {

    @Test
    @DisplayName("실패 - 알림이 존재하지 않는 경우 NOTIFICATION_NOT_FOUND 예외가 발생한다")
    void fail_notFound() {
      // Given
      UUID userId = UUID.randomUUID();
      UUID notificationId = UUID.randomUUID();
      when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> notificationService.confirmNotification(userId, notificationId))
          .isInstanceOf(BaseException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 알림의 소유자가 아닌 경우 NOTIFICATION_ACCESS_DENIED 예외가 발생한다")
    void fail_accessDenied() {
      // Given
      UUID userId = UUID.randomUUID();
      UUID otherUserId = UUID.randomUUID();
      UUID notificationId = UUID.randomUUID();
      Notification notification = Notification.builder()
          .id(notificationId)
          .userId(otherUserId)
          .build();

      when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

      // When & Then
      assertThatThrownBy(() -> notificationService.confirmNotification(userId, notificationId))
          .isInstanceOf(BaseException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("성공 - 알림을 확인 상태로 변경한다")
    void success() {
      // Given
      UUID userId = UUID.randomUUID();
      UUID notificationId = UUID.randomUUID();
      Notification notification = Notification.builder()
          .id(notificationId)
          .userId(userId)
          .build();

      when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
      doNothing().when(notificationRepository).updateConfirmStatus(notificationId, true);

      // When
      notificationService.confirmNotification(userId, notificationId);

      // Then
      verify(notificationRepository).updateConfirmStatus(notificationId, true);
    }
  }

  @Nested
  @DisplayName("confirmAllNotifications 테스트")
  class ConfirmAllNotifications {

    @Test
    @DisplayName("성공 - 사용자의 모든 알림을 확인 상태로 변경한다")
    void success() {
      // Given
      UUID userId = UUID.randomUUID();
      doNothing().when(notificationRepository).updateAllConfirmStatusByUserId(userId, true);

      // When
      notificationService.confirmAllNotifications(userId);

      // Then
      verify(notificationRepository).updateAllConfirmStatusByUserId(userId, true);
    }
  }

  @Nested
  @DisplayName("deleteOldConfirmedNotifications 테스트")
  class DeleteOldConfirmedNotifications {

    @Test
    @DisplayName("성공 - 7일 이전의 확인된 알림을 삭제한다")
    void success() {
      // Given
      ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);

      // When
      notificationService.deleteOldConfirmedNotifications();

      // Then
      verify(notificationRepository).deleteConfirmedNotificationsOlderThan(instantCaptor.capture());
      Instant captured = instantCaptor.getValue();
      Instant now = Instant.now();
      
      assertThat(captured).isBefore(now);
      assertThat(ChronoUnit.DAYS.between(captured, now)).isEqualTo(7);
    }
  }

  @Nested
  @DisplayName("createNotification 테스트")
  class CreateNotification {

    @Test
    @DisplayName("성공 - 알림을 생성하고 저장한다")
    void success() {
      // Given
      UUID userId = UUID.randomUUID();
      String content = "새로운 댓글이 달렸습니다.";
      String resourceType = "COMMENT";
      UUID resourceId = UUID.randomUUID();

      ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
      when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      notificationService.createNotification(userId, content, resourceType, resourceId);

      // Then
      verify(notificationRepository).save(notificationCaptor.capture());
      Notification saved = notificationCaptor.getValue();
      assertThat(saved.getId()).isNotNull();
      assertThat(saved.getUserId()).isEqualTo(userId);
      assertThat(saved.getContent()).isEqualTo(content);
      assertThat(saved.getResourceType()).isEqualTo(resourceType);
      assertThat(saved.getResourceId()).isEqualTo(resourceId);
      assertThat(saved.isConfirmed()).isFalse();
    }
  }
}
