package com.codeit.server.interest.dto;

import com.codeit.server.interest.entity.Subscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionResponse {

    private UUID id;
    private UUID userId;
    private UUID interestId;
    private String interestName;
    private LocalDateTime createdAt;

    public static SubscriptionResponse from(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())
                .interestId(subscription.getInterest().getId())
                .interestName(subscription.getInterest().getName())
                .createdAt(LocalDateTime.ofInstant(subscription.getCreatedAt(), ZoneId.systemDefault()))
                .build();
    }
}