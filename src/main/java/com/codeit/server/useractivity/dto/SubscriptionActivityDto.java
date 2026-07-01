package com.codeit.server.useractivity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionActivityDto {

  private final String id;
  private final String interestId;
  private final String interestName;
  private final List<String> interestKeywords;
  private final long interestSubscriberCount;
  private final String createdAt;

}
