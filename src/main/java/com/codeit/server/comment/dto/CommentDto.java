package com.codeit.server.comment.dto;

import com.codeit.server.comment.entity.Comment;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CommentDto {

  private UUID id;
  private UUID articleId;
  private UUID userId;
  private String userNickname;
  private String content;
  private long likeCount;
  private boolean likedByMe;
  private Instant createdAt;

  public static CommentDto from(Comment comment, String userNickname, boolean likedByMe) {
    return CommentDto.builder()
        .id(comment.getId())
        .articleId(comment.getArticleId())
        .userId(comment.getUserId())
        .userNickname(userNickname)
        .content(comment.getContent())
        .likeCount(comment.getLikeCount())
        .likedByMe(likedByMe)
        .createdAt(comment.getCreatedAt())
        .build();
  }




}
