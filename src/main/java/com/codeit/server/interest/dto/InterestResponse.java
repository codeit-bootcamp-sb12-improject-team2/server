package com.codeit.server.interest.dto;

import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.InterestKeyword;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class InterestResponse {

    private UUID id;
    private String name;
    private Integer subscriberCount;
    private List<String> keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InterestResponse from(Interest interest) {
        return InterestResponse.builder()
                .id(interest.getId())
                .name(interest.getName())
                .subscriberCount(interest.getSubscriberCount())
                .keywords(interest.getKeywords().stream()
                        .map(InterestKeyword::getKeyword)
                        .toList())
                .createdAt(LocalDateTime.ofInstant(interest.getCreatedAt(), ZoneOffset.UTC))
                .updatedAt(LocalDateTime.ofInstant(interest.getUpdatedAt(), ZoneOffset.UTC))
                .build();
    }
}