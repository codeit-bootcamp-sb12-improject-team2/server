package com.codeit.server.useractivity.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserActivityDto {

  private final String id;
  private final String email;
  private final String nickname;
  private final String createdAt;
  private final List<SubscriptionActivityDto> subscriptions;
  private final List<CommentUserActivityDto> comments;
  private final List<CommentLikeUserActivityDto> commentLikes;
  private final List<ArticleActivityDto> articleViews;

}
