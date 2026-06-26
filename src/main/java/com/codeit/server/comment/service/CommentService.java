package com.codeit.server.comment.service;

import com.codeit.server.comment.entity.Comment;
import com.codeit.server.comment.entity.CommentLike;
import com.codeit.server.comment.repository.CommentLikeRepository;
import com.codeit.server.comment.repository.CommentRepository;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;

  public Comment create(UUID articleId, UUID userId, String content) {
    Comment comment = Comment.builder()
        .articleId(articleId)
        .userId(userId)
        .content(content)
        .build();

    // Article 도메인 구현 후 commentCount 증가 처리
    return commentRepository.save(comment);
  }

  public Comment update(UUID commentId, UUID userId, String content) {
    Comment comment = getComment(commentId);
    validateOwner(comment, userId);
    validateNotDeleted(comment);

    comment.updateContent(content);
    return comment;
  }

  public void delete(UUID commentId, UUID userId) {
    Comment comment = getComment(commentId);
    validateOwner(comment, userId);
    validateNotDeleted(comment);

    comment.delete();
  }

  public void hardDelete(UUID commentId, UUID userId) {
    Comment comment = getComment(commentId);
    validateOwner(comment, userId);

    commentRepository.delete(comment);
  }

  public CommentLike like(UUID commentId, UUID userId) {
    Comment comment = getComment(commentId);
    validateNotDeleted(comment);

    if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
      throw new BaseException(ErrorCode.ALREADY_LIKED_COMMENT);
    }

    CommentLike commentLike = CommentLike.builder()
        .userId(userId)
        .commentId(commentId)
        .build();

    comment.increaseLikeCount();
    return commentLikeRepository.save(commentLike);
  }

  public void unlike(UUID commentId, UUID userId) {
    Comment comment = getComment(commentId);
    validateNotDeleted(comment);

    CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_LIKE_NOT_FOUND));

    comment.decreaseLikeCount();
    commentLikeRepository.delete(commentLike);
  }

  @Transactional(readOnly = true)
  public Comment get(UUID commentId) {
    return getComment(commentId);
  }

  private Comment getComment(UUID commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
  }

  private void validateOwner(Comment comment, UUID userId) {
    if (!comment.getUserId().equals(userId)) {
      throw new BaseException(ErrorCode.COMMENT_ACCESS_DENIED);
    }
  }

  private void validateNotDeleted(Comment comment) {
    if (comment.isDeleted()) {
      throw new BaseException(ErrorCode.COMMENT_ALREADY_DELETED);
    }
  }
}