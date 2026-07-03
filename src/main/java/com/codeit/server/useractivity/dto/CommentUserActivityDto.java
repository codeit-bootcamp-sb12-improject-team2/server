package com.codeit.server.useractivity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommentUserActivityDto {

  private final String id;
  private final String articleId;
  private final String articleTitle;
  private final String userId;
  private final String userNickname;
  private final String content;
  private final long likeCount;
  private final String createdAt;

}
