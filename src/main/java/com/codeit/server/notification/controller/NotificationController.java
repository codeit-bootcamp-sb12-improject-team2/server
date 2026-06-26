package com.codeit.server.notification.controller;


import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.server.notification.service.NotificationService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
      @RequestHeader("Monew-User-Id") UUID userId,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "limit", defaultValue = "50") int limit
  ) {
    Instant afterInstant = (after != null && !after.isEmpty()) ? Instant.parse(after) : null;

    CursorPageResponseNotificationDto response =
        notificationService.getUnconfirmedNotifications(userId, cursor, afterInstant, limit);
    return ResponseEntity.ok(response);
  }

}
