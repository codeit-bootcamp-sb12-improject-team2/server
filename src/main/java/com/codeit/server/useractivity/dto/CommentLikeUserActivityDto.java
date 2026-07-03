package com.codeit.server.useractivity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommentLikeUserActivityDto {

  private final String id;
  private final String createdAt;
  private final String commentId;
  private final String articleId;
  private final String articleTitle;
  private final String commentUserId;
  private final String commentUserNickname;
  private final String commentContent;
  private final long commentLikeCount;
  private final String commentCreatedAt;

}
