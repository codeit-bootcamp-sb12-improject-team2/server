package com.codeit.server.comment.entity;

import com.codeit.server.global.common.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Comment extends BaseUpdatableEntity {

  @Column(name = "article_id", columnDefinition = "uuid", nullable = false)
  private UUID articleId;

  @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
  private UUID userId;

  @Column(name = "content", length = 255, nullable = false)
  private String content;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;

  public void updateContent(String content) {
    this.content = content;
  }

  public void delete() {
    this.isDeleted = true;
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }
}
