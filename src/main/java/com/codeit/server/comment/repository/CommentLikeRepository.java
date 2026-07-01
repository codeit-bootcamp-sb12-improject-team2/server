package com.codeit.server.comment.repository;

import com.codeit.server.comment.entity.CommentLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

  boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);

  Optional<CommentLike> findByUserIdAndCommentId(UUID userId, UUID commentId);

  void deleteByCommentId(UUID commentId);
}