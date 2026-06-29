package com.codeit.server.notification.controller;


import com.codeit.server.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.server.notification.service.NotificationService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
      @RequestParam(value = "cursor", required = false) UUID cursor,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(value = "limit", defaultValue = "50") int limit
  ) {

    CursorPageResponseNotificationDto response =
        notificationService.getUnconfirmedNotifications(userId, cursor, after, limit);
    return ResponseEntity.ok(response);
  }

}
