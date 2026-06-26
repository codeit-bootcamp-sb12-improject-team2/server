package com.codeit.server.comment.entity;

import com.codeit.server.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(
    name = "comment_likes",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_comment_likes_user_comment",
        columnNames = {"user_id", "comment_id"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class CommentLike extends BaseEntity {

  @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
  private UUID userId;

  @Column(name = "comment_id", columnDefinition = "uuid", nullable = false)
  private UUID commentId;
}
