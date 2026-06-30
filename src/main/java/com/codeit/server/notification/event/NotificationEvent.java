package com.codeit.server.notification.event;

import java.util.UUID;

public record NotificationEvent(
    UUID userId,
    String content,
    String resourceType,
    UUID resourceId
) {
}
