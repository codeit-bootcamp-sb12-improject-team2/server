package com.codeit.server.interest.dto;

import com.codeit.server.interest.entity.InterestKeyword;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InterestKeywordResponse {

    private UUID id;
    private UUID interestId;
    private String keyword;

    public static InterestKeywordResponse from(InterestKeyword interestKeyword) {
        return InterestKeywordResponse.builder()
                .id(interestKeyword.getId())
                .interestId(interestKeyword.getInterest().getId())
                .keyword(interestKeyword.getKeyword())
                .build();
    }
}