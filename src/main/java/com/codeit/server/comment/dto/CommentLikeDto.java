package com.codeit.server.comment.dto;

import com.codeit.server.comment.entity.Comment;
import com.codeit.server.comment.entity.CommentLike;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentLikeDto {

  private UUID id;
  private UUID likedBy;
  private Instant createdAt;
  private UUID commentId;
  private UUID articleId;
  private UUID commentUserId;
  private String commentUserNickname;
  private String commentContent;
  private long commentLikeCount;
  private Instant commentCreatedAt;

  public static CommentLikeDto from(
      CommentLike commentLike,
      Comment comment,
      String commentUserNickname
  ) {
    return CommentLikeDto.builder()
        .id(commentLike.getId())
        .likedBy(commentLike.getUserId())
        .createdAt(commentLike.getCreatedAt())
        .commentId(comment.getId())
        .articleId(comment.getArticleId())
        .commentUserId(comment.getUserId())
        .commentUserNickname(commentUserNickname)
        .commentContent(comment.getContent())
        .commentLikeCount(comment.getLikeCount())
        .commentCreatedAt(comment.getCreatedAt())
        .build();
  }

}
