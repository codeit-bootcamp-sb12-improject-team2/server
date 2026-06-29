package com.codeit.server.interest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class SubscriptionRequest {

    @NotNull
    private UUID interestId;
}