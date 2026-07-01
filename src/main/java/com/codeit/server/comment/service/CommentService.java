package com.codeit.server.comment.service;

import com.codeit.server.article.entity.Article;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.comment.dto.CommentDto;
import com.codeit.server.comment.dto.CommentLikeDto;
import com.codeit.server.comment.dto.CursorPageResponseCommentDto;
import com.codeit.server.comment.entity.Comment;
import com.codeit.server.comment.entity.CommentLike;
import com.codeit.server.comment.repository.CommentLikeRepository;
import com.codeit.server.comment.repository.CommentRepository;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final CommentModerationService commentModerationService;


  public CommentDto create(UUID articleId, UUID userId, String content) {
    commentModerationService.validate(content);

    User user = getUser(userId);
    Article article = getArticle(articleId);

    Comment comment = Comment.builder()
        .articleId(articleId)
        .userId(userId)
        .content(content)
        .build();

    article.increaseCommentCount();
    Comment savedComment = commentRepository.save(comment);
    return CommentDto.from(savedComment, user.getNickname(), false);
  }

  public CommentDto update(UUID commentId, UUID userId, String content) {
    Comment comment = getComment(commentId);
    validateOwner(comment, userId);
    validateNotDeleted(comment);

    commentModerationService.validate(content);
    comment.updateContent(content);

    User user = getUser(comment.getUserId());
    boolean likedByMe = commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
    return CommentDto.from(comment, user.getNickname(), likedByMe);
  }

  public void delete(UUID commentId) {
    Comment comment = getComment(commentId);
    validateNotDeleted(comment);

    Article article = getArticle(comment.getArticleId());
    article.decreaseCommentCount();

    comment.delete();
  }

  public void hardDelete(UUID commentId) {
    Comment comment = getComment(commentId);
    Article article = getArticle(comment.getArticleId());

    if(!comment.isDeleted()){
      article.decreaseCommentCount();
    }

    commentLikeRepository.deleteByCommentId(commentId);
    commentRepository.delete(comment);
  }

  public CommentLikeDto like(UUID commentId, UUID userId) {
    Comment comment = getComment(commentId);
    validateNotDeleted(comment);
    getUser(userId);

    if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
      throw new BaseException(ErrorCode.ALREADY_LIKED_COMMENT);
    }

    CommentLike commentLike = CommentLike.builder()
        .userId(userId)
        .commentId(commentId)
        .build();

    comment.increaseLikeCount();
    CommentLike savedCommentLike = commentLikeRepository.save(commentLike);
    User commentUser = getUser(comment.getUserId());
    return CommentLikeDto.from(savedCommentLike, comment, commentUser.getNickname());
  }

  public void unlike(UUID commentId, UUID userId) {
    Comment comment = getComment(commentId);
    validateNotDeleted(comment);
    getUser(userId);

    CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_LIKE_NOT_FOUND));

    comment.decreaseLikeCount();
    commentLikeRepository.delete(commentLike);
  }

  @Transactional(readOnly = true)
  public CursorPageResponseCommentDto getComments(
      UUID articleId,
      UUID requestUserId,
      String orderBy,
      Sort.Direction direction,
      String cursor,
      Instant after,
      int limit
  ) {
    getArticle(articleId);
    getUser(requestUserId);

    return commentRepository.findAllByArticle(
        articleId,
        requestUserId,
        orderBy,
        direction,
        cursor,
        after,
        limit
    );
  }


  @Transactional(readOnly = true)
  public Comment get(UUID commentId) {
    return getComment(commentId);
  }

  private Comment getComment(UUID commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
  }

  private User getUser(UUID userId) {
    return userRepository.findByIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
  }

  private Article getArticle(UUID articleId) {
    return articleRepository.findById(articleId)
        .orElseThrow(() -> new BaseException(ErrorCode.ARTICLE_NOT_FOUND));
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
