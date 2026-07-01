package com.codeit.server.useractivity.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserActivityDto {

  private final String id;
  private final String email;
  private final String nickname;
  private final Instant createdAt;
  private final List<SubscriptionActivityDto> subscriptions;
  private final List<CommentUserActivityDto> comments;
  private final List<CommentLikeUserActivityDto> commentLikes;
  private final List<ArticleActivityDto> articleViews;

}
